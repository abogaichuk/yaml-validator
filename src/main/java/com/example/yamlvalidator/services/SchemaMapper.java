package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.Position;
import com.example.yamlvalidator.entity.Schema;
import com.example.yamlvalidator.errors.PadmGrammarException;
import com.example.yamlvalidator.utils.ValidatorUtils;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.nodes.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.yamlvalidator.grammar.KeyWord.*;
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
                .filter(nodeTuple -> TYPES.lowerCase().equals(getName(nodeTuple.getKeyNode())))
                .findAny().orElse(null);;
    }

    public Schema map() {
        return build(EMPTY, EMPTY, null, Position.of(1, 1), Parameter.YamlType.MAPPING, rootNode);
    }

    private Schema build(String name, String value, Schema parent, Parameter.YamlType type) {
        return build(name, value, parent, null, type);
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
                         Parameter.YamlType type, Node node) {
        var schema = build(name, value, parent, position, type);
        if (parent != null) {
            parent.deleteIfPresent(name);
        }

        buildChildren(node, schema).ifPresent(schema::addChildren);
        return schema;
    }

    private Optional<List<Schema>> buildChildren(Node node, Schema parent) {
        return Optional.ofNullable(node)
                .map(n -> {
                    if (n instanceof CollectionNode) {
                        return toParameters((CollectionNode<?>) node, parent);
                    } else {
                        return List.of(build(TYPE.name().toLowerCase(), ((ScalarNode) node).getValue(),
                                parent, null, Parameter.YamlType.SCALAR));
                    }
                });
    }

    private List<Schema> toParameters(final CollectionNode<?> node, final Schema parent) {
        return node.getValue().stream()
                .map(o -> {
                    if (o instanceof NodeTuple) {
                        var tuple = (NodeTuple) o;
                        if (TYPES.lowerCase().equals(getName(tuple.getKeyNode())))
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
            return build(paramName, EMPTY, parent, position, Parameter.YamlType.MAPPING, valueNode);
        } else if (SCALAR.equals(valueNode.getNodeType())) {
            return scalarParsing(paramName, ((ScalarNode) valueNode).getValue(), parent, position);
        } else if (SEQUENCE.equals(valueNode.getNodeType())) {
            return build(paramName, EMPTY, parent, position, Parameter.YamlType.SEQUENCE, valueNode);
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
                            //todo debug case
                            return build(paramName, value, parent, position, Parameter.YamlType.MAPPING, node);
                        } else {
                            return build(paramName, EMPTY, parent, position, Parameter.YamlType.SEQUENCE, node);
                        }
                    })
                    .orElseThrow(() -> new PadmGrammarException(getMessage(MESSAGE_UNKNOWN_TYPE, paramName, value)));
        } else if (match(paramName)) {
            return resolve(paramName, parent, position, value.split(OR_TYPE_SPLITTER));
        }
        return build(paramName, value, parent, position, Parameter.YamlType.SCALAR);
    }

    private boolean match(String paramName) {
        return isTypeKeyWord(paramName) || (isNotEmpty(paramName) && isNotAKeyword(paramName));
    }

    //if it's a type node - update parent, in other case create new node
    private Schema resolve(String name, Schema parent, Position position, String[] types) {
        if (isTheTypeNode(name)) {
            if (types.length > 1) {
                setOneOf(parent, types);
            } else {
                parent.addChildren(resolveChildren(name, types[0], parent));
            }
            return null; //return null because we want to delete type section and update its parent
        } else {
            return types.length > 1 ? resolveOneOf(name, parent, position, types) : resolveParam(name, types[0], parent);
        }
    }

    private Schema resolveOneOf(String name, Schema parent, Position position, String[] types) {
        var param = build(name, EMPTY, parent, position, Parameter.YamlType.MAPPING);
        setOneOf(param, types);
        return param;
    }

    private boolean isTheTypeNode(String name) {
        return TYPE.lowerCase().equals(name);
    }

    private Schema resolveParam(String name, String type, Schema parent) {
        return getStandardType(type)
                .map(tuple -> build(name, EMPTY, parent, null, Parameter.YamlType.MAPPING, tuple.getValueNode()))
                .orElseGet(() -> getCustomType(type)
                        .map(tuple -> build(name, EMPTY, parent, null, Parameter.YamlType.MAPPING, tuple.getValueNode()))
                        .orElseThrow(() -> new PadmGrammarException(getMessage(MESSAGE_UNKNOWN_TYPE, parent, type))));
    }

    private List<Schema> resolveChildren(String name, String type, Schema parent) {
        return getStandardType(type)
                .map(tuple -> List.of(build(name, type, parent, Parameter.YamlType.SCALAR)))
                .orElseGet(() -> getCustomType(type)
                        .flatMap(tuple -> buildChildren(tuple.getValueNode(), parent))
                        .orElseThrow(() -> new PadmGrammarException(getMessage(MESSAGE_UNKNOWN_TYPE, parent, type))));
    }

    private void setOneOf(Schema parent, String[] types) {
        var oneOf = build(ONEOF.lowerCase(), EMPTY, parent, Parameter.YamlType.SEQUENCE);
        var children = Stream.of(types)
                .map(String::trim)
                .map(type -> resolveParam(EMPTY, type, oneOf))
                .collect(Collectors.toList());
        oneOf.addChildren(children);
        parent.addChild(oneOf);
    }

    private Optional<NodeTuple> getStandardType(String type) {
        return Optional.ofNullable(type)
                .filter(ValidatorUtils::isStandardType)
                .map(t -> new NodeTuple(
                        new ScalarNode(Tag.STR, TYPE.name().toLowerCase(), ScalarStyle.PLAIN),
                        new ScalarNode(Tag.STR, t, ScalarStyle.PLAIN)));
    }

    private Optional<NodeTuple> getCustomType(String type) {
        return Optional.ofNullable(typesNode)
                .map(NodeTuple::getValueNode)
                .map(MappingNode.class::cast)
                .map(MappingNode::getValue)
                .flatMap(types -> types.stream()
                        .filter(tuple -> getName(tuple.getKeyNode()).equalsIgnoreCase(type))
                        .findAny());
    }
}
