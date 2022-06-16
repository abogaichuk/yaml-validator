package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.Position;
import com.example.yamlvalidator.entity.Resource;
import com.example.yamlvalidator.entity.Schema;
import com.example.yamlvalidator.errors.PadmGrammarException;
import com.example.yamlvalidator.grammar.KeyWord;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.nodes.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.yamlvalidator.grammar.KeyWord.ONEOF;
import static com.example.yamlvalidator.grammar.KeyWord.TYPES;
import static com.example.yamlvalidator.utils.MappingUtils.getName;
import static com.example.yamlvalidator.utils.MappingUtils.getPosition;
import static com.example.yamlvalidator.utils.MessagesUtils.MESSAGE_UNKNOWN_TYPE;
import static com.example.yamlvalidator.utils.MessagesUtils.getMessage;
import static com.example.yamlvalidator.utils.ValidatorUtils.*;
import static org.snakeyaml.engine.v2.nodes.NodeType.*;

public class SchemaMapper {
    private final PlaceHolderResolver placeHolderResolver;
    private final MappingNode rootNode;
    private final NodeTuple typesNode;

    public SchemaMapper(MappingNode node) {
        Objects.requireNonNull(node);
        this.rootNode = node;
        this.placeHolderResolver = new PlaceHolderResolver(node);
        this.typesNode = node.getValue().stream()
                .filter(nodeTuple -> TYPES.name().equalsIgnoreCase(getName(nodeTuple.getKeyNode())))
                .findAny().orElse(null);
    }

    public Schema map() {
        return build(EMPTY, EMPTY, null, Position.of(1, 1), Parameter.YamlType.MAPPING, rootNode);
    }

    private Schema build(String name, String value, Schema parent, Position position, Parameter.YamlType type) {
        return Schema.builder()
                .name(name)
                .value(value)
                .parent(parent)
                .position(position)
                .yamlType(type).build();
    }

    private Schema build(String name, String value, Schema parent, Position position,
                         Parameter.YamlType type, CollectionNode<?> node) {
        var schema = build(name, value, parent, position, type);
        if (node != null) {
            schema.addChildren(toParameters(node, schema));
        }
        return schema;
    }

    //todo do we need the parent?
    private List<Schema> toParameters(final CollectionNode<?> node, final Schema parent) {
        return node.getValue().stream()
                .map(o -> {
                    if (o instanceof NodeTuple) {
                        var tuple = (NodeTuple) o;
                        if (TYPES.name().equalsIgnoreCase(getName(tuple.getKeyNode())))
                            return null; //skip types section
                        return toParameter(tuple.getKeyNode(), tuple.getValueNode(), parent);
                    } else if (o instanceof Node) {
                        return toParameter(null, (Node) o, parent);
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Schema toParameter(final Node keyNode, final Node valueNode, final Schema parent) {
        var paramName = getName(keyNode);
        var position = getPosition(keyNode);

        if (MAPPING.equals(valueNode.getNodeType())) {
            return build(paramName, EMPTY, parent, position, Parameter.YamlType.MAPPING, (MappingNode) valueNode);
        } else if (SCALAR.equals(valueNode.getNodeType())) {
            return scalarParsing(paramName, ((ScalarNode) valueNode).getValue(), parent, position);
        } else if (SEQUENCE.equals(valueNode.getNodeType())) {
            return build(paramName, EMPTY, parent, position, Parameter.YamlType.SEQUENCE, (SequenceNode) valueNode);
        } else {
            throw new PadmGrammarException("unknown node type: " + valueNode.getNodeType());
        }
    }

    private Schema scalarParsing(String paramName, String value, Schema parent, Position position) {
        //todo why it's not a keyword, quick fix to avoid placeholder in description?
        if (isNotAKeyword(paramName) && placeHolderResolver.match(value)) {
            //todo what if placeholder in description value? it's not a node
            return placeHolderResolver.resolve(value)
                    .map(node -> {
                        if (SCALAR.equals(node.getNodeType())) {
                            return build(paramName, ((ScalarNode) node).getValue(), parent, position, Parameter.YamlType.SCALAR);
                        } else if (MAPPING.equals(node.getNodeType())) {
                            return build(paramName, value, parent, position, Parameter.YamlType.MAPPING, (MappingNode) node);
                        } else {
                            return build(paramName, EMPTY, parent, position, Parameter.YamlType.SEQUENCE, (MappingNode) node);
                        }
                    })
                    .orElseThrow(() -> new PadmGrammarException(getMessage(MESSAGE_UNKNOWN_TYPE, paramName, value)));
        } else if (typeResolvingIsNeeded(paramName, value)) {
            var resolved = resolveTypes(paramName, parent, value.split(OR_TYPE_SPLITTER));
            return resolved;
        }
        return build(paramName, value, parent, null, Parameter.YamlType.SCALAR);
    }

    private Schema resolveTypes(String name, Schema parent, String[] types) {
        if (types.length > 1) {
            if (KeyWord.TYPE.name().equalsIgnoreCase(name)) {
                var oneOf = build(KeyWord.ONEOF.name().toLowerCase(), EMPTY, parent, null, Parameter.YamlType.SEQUENCE);
                var children = Stream.of(types)
                        .map(String::trim)
                        .map(type -> resolveType("", oneOf, type))
                        .collect(Collectors.toList());
                oneOf.addChildren(children);
                parent.addChild(oneOf);
                return null;
            } else {
                var param = build(name, EMPTY, parent, null, Parameter.YamlType.MAPPING);
                var oneOf = build(ONEOF.name().toLowerCase(), EMPTY, param, null, Parameter.YamlType.SEQUENCE);
                var children = Stream.of(types)
                        .map(String::trim)
                        .map(type -> resolveType("", oneOf, type))
                        .collect(Collectors.toList());
                oneOf.addChildren(children);
                param.addChild(oneOf);
                return param;
            }
        } else {
            if (KeyWord.TYPE.name().equalsIgnoreCase(name)) {
                var children = resolveType(parent.getName(), (Schema) parent.getParent(), types[0]).getChildren()
                        .map(Schema.class::cast)
                        .collect(Collectors.toList());
                parent.addChildren(children);
                return null;
            } else {
                Schema param = resolveType(name, parent, types[0]);
                return param;
            }
        }
    }

    private Schema resolveType(String name, Schema parent, String type) {
        if (isStandardType(type)) {
            return build("", type, parent, null, Parameter.YamlType.SCALAR);
        } else {
            Optional<NodeTuple> customType = findCustomType(type);
            Schema schemaParams = customType
                    .map(nodeTuple -> toParameter(new ScalarNode(Tag.STR, name, ScalarStyle.PLAIN), nodeTuple.getValueNode(), parent))
                    .orElseThrow(() -> new PadmGrammarException(getMessage(MESSAGE_UNKNOWN_TYPE, parent, type)));
            return schemaParams;
        }
    }

    private Optional<NodeTuple> findCustomType(String type) {
        return Optional.ofNullable(typesNode)
                .map(NodeTuple::getValueNode)
                .map(MappingNode.class::cast)
                .map(MappingNode::getValue)
                .flatMap(types -> types.stream()
                        .filter(tuple -> getName(tuple.getKeyNode()).equalsIgnoreCase(type))
                        .findAny());
    }

    //if paramname == type or paramname is not a keyword(custom type) and value is not a standard type
    private boolean typeResolvingIsNeeded(String paramName, String typeValue) {
        return (isTypeKeyWord(paramName) || (isNotEmpty(paramName) && isNotAKeyword(paramName))) && isNotAStandardType(typeValue);
    }
}
