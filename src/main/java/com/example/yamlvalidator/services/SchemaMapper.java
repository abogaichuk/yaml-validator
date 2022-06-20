package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.Position;
import com.example.yamlvalidator.entity.Schema;
import com.example.yamlvalidator.errors.PadmGrammarException;
import com.example.yamlvalidator.grammar.KeyWord;
import com.example.yamlvalidator.utils.ValidatorUtils;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.nodes.*;

import java.util.Collections;
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
    private final CustomTypesResolver customTypesResolver;
    private final MappingNode rootNode;
    private final NodeTuple typesNode;

    public SchemaMapper(MappingNode node) {
        Objects.requireNonNull(node);
        this.rootNode = node;
        this.placeHolderResolver = new PlaceHolderResolver(node);
        this.typesNode = node.getValue().stream()
                .filter(nodeTuple -> TYPES.name().equalsIgnoreCase(getName(nodeTuple.getKeyNode())))
                .findAny().orElse(null);
        this.customTypesResolver = new CustomTypesResolver(typesNode);
    }

    public Schema map() {
        var schema = build(EMPTY, EMPTY, null, Position.of(1, 1), Parameter.YamlType.MAPPING, rootNode);
        return schema;
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
        if (node != null) {
            if (node instanceof CollectionNode) {
                schema.addChildren(toParameters((CollectionNode<?>) node, schema));
            } else {
                var child = build(TYPE.name().toLowerCase(), ((ScalarNode) node).getValue(),
                        schema, null, Parameter.YamlType.SCALAR);
                schema.addChild(child);
            }
        }
        return schema;
    }

//    private Schema build(String name, String value, Schema parent, Position position,
//                         Parameter.YamlType type, CollectionNode<?> node) {
//        var schema = build(name, value, parent, position, type);
//        if (parent != null) {
//            parent.deleteIfPresent(name);
//        }
//        if (node != null) {
//            schema.addChildren(toParameters(node, schema));
//        }
//        return schema;
//    }

    //todo do we need the parent? we need because of we need parent for type section parsing
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
                            //todo debug case
                            return build(paramName, value, parent, position, Parameter.YamlType.MAPPING, (MappingNode) node);
                        } else {
                            return build(paramName, EMPTY, parent, position, Parameter.YamlType.SEQUENCE, (MappingNode) node);
                        }
                    })
                    .orElseThrow(() -> new PadmGrammarException(getMessage(MESSAGE_UNKNOWN_TYPE, paramName, value)));
        } else if (match(paramName)) {
            var resolved = resolve(paramName, parent, position, value.split(OR_TYPE_SPLITTER));
            return resolved;
        }
        return build(paramName, value, parent, null, Parameter.YamlType.SCALAR);
    }

    private boolean match(String paramName) {
        return isTypeKeyWord(paramName) || (isNotEmpty(paramName) && isNotAKeyword(paramName));
    }

    private Schema resolve(String name, Schema parent, Position position, String[] types) {
        if (types.length > 1) {
            if (KeyWord.TYPE.name().equalsIgnoreCase(name)) {
                setOneOf(parent, types);
                return null; //return null because we want to delete type section and update its parent
            } else {
                var param = build(name, EMPTY, parent, position, Parameter.YamlType.MAPPING);
                setOneOf(param, types);
                return param;
            }
        } else {
            var type = types[0];

            if (KeyWord.TYPE.name().equalsIgnoreCase(name)) {
                if (isStandardType(type)) {
                    parent.addChild(build(name, type, parent, position, Parameter.YamlType.SCALAR));
                } else {
                    var customTypes = mapCustomParams(name, parent, type, position);
                    parent.addChildren(customTypes);
                }
                return null;
            } else {
                if (isStandardType(type)) {
                    var param = build(name, EMPTY, parent, position, Parameter.YamlType.MAPPING);
                    param.addChild(build(TYPE.name().toLowerCase(), type, param, position, Parameter.YamlType.SCALAR));
                    return param;
//                    return build(EMPTY, type, parent, position, Parameter.YamlType.SCALAR);
                } else {
                    var param = build(name, EMPTY, parent, position, Parameter.YamlType.MAPPING);
                    var children = getCustomType(type)
                            .map(tuple -> {
                                if (tuple.getValueNode() instanceof CollectionNode) {
                                    return toParameters((MappingNode) tuple.getValueNode(), param);
                                } else {
                                    return List.of(build(TYPE.name().toLowerCase(), ((ScalarNode) tuple.getValueNode()).getValue(), param, position, Parameter.YamlType.SCALAR));
//                                    Schema schema = toParameter(new ScalarNode(Tag.STR, TYPE.name(), ScalarStyle.PLAIN), tuple.getValueNode(), param);
//                                    return List.of(schema);
                                }
                            }).orElseThrow(() -> new PadmGrammarException(getMessage(MESSAGE_UNKNOWN_TYPE, param, type)));
                    param.addChildren(children);
//                    findCustomType(type)
//                            .map(tuple -> build(name, type, parent, position, ))
//                    var r = customTypes.isEmpty() ? null : customTypes.get(0);
//                    var r = resolveType(name, parent, type, position);
//                    return r;
//                    return resolveType(name, parent, type, position);
                    return param;
                }
            }
        }
    }

    private List<Schema> mapCustomParams(String name, Schema parent, String type, Position position) {
        return getCustomType(type)
                .map(tuple -> {
                    if (tuple.getValueNode() instanceof CollectionNode) {
                        return toParameters((MappingNode) tuple.getValueNode(), parent);
                    } else {
                        var a = build(TYPE.name().toLowerCase(), ((ScalarNode) tuple.getValueNode()).getValue(),
                                parent, position, Parameter.YamlType.SCALAR);
                        return List.of(a);
//                        return List.of(toParameter(new ScalarNode(Tag.STR, name, ScalarStyle.PLAIN), tuple.getValueNode(), parent));
                    }
                }).orElseThrow(() -> new PadmGrammarException(getMessage(MESSAGE_UNKNOWN_TYPE, parent, type)));
    }

    private void setOneOf(Schema parent, String[] types) {
        var oneOf = build(ONEOF.name().toLowerCase(), EMPTY, parent, null, Parameter.YamlType.SEQUENCE);
        var children = Stream.of(types)
                .map(String::trim)
                .map(type -> resolveType(type, oneOf))
                .collect(Collectors.toList());
        oneOf.addChildren(children);
        parent.addChild(oneOf);
    }

    private Schema resolveType(String type, Schema parent) {
        return getStandardType(type)
                .map(tuple -> build(EMPTY, EMPTY, parent, null, Parameter.YamlType.MAPPING, tuple.getValueNode()))
                .orElseGet(() -> getCustomType(type)
                        .map(tuple -> build(EMPTY, EMPTY, parent, null, Parameter.YamlType.MAPPING,
                                tuple.getValueNode()))
                        .orElseThrow(() -> new PadmGrammarException(getMessage(MESSAGE_UNKNOWN_TYPE, parent, type))));
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
