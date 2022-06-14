package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.Position;
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

import static com.example.yamlvalidator.grammar.KeyWord.TYPES;
import static com.example.yamlvalidator.utils.MessagesUtils.MESSAGE_UNKNOWN_TYPE;
import static com.example.yamlvalidator.utils.MessagesUtils.getMessage;
import static com.example.yamlvalidator.utils.ValidatorUtils.*;
import static org.snakeyaml.engine.v2.nodes.NodeType.*;

public class SchemaMapper implements YamlMapper<Schema> {
    private final PlaceHolderResolver placeHolderResolver;
    private Optional<NodeTuple> typesNode;

    public SchemaMapper(PlaceHolderResolver placeHolderResolver) {
        this.placeHolderResolver = placeHolderResolver;
    }

    @Override
    public Schema toParameter(Node node) {
        var root = (MappingNode) node;
        typesNode = root.getValue().stream()
                .filter(nodeTuple -> TYPES.name().equalsIgnoreCase(getKey(nodeTuple)))
                .findAny();

        return build(Position.of(1, 1), Parameter.YamlType.MAPPING, root);
    }

    private Schema build(Position position, Parameter.YamlType type, MappingNode node) {
        return build("", "", null, position, type, node);
    }

    private Schema build(Schema parent, Position position, Parameter.YamlType type, MappingNode node) {
        return build("", "", parent, position, type, node);
    }

    private Schema build(String name, String value, Schema parent, Position position, Parameter.YamlType type) {
        return build(name, value, parent, position, type, null);
    }

    private Schema build(String name, String value, Schema parent, Position position,
                         Parameter.YamlType type, MappingNode node) {
        var schema = new Schema(name, value, parent, position, type);
        if (node != null) {
            schema.addChildren(toParameters(node, schema).collect(Collectors.toList()));
        }
        return schema;
    }

    //todo do we need the parent?
    private Stream<Schema> toParameters(final MappingNode node, final Schema parent) {
        return node.getValue().stream()
                .filter(tuple -> !TYPES.name().equals(getKey(tuple)))
                .map(n -> toParameter(n, parent))
                .filter(Objects::nonNull);
    }

    private Schema toParameter(final NodeTuple tuple, final Schema parent) {
        var paramName = getKey(tuple).toLowerCase();
        var position = getPosition(tuple.getKeyNode());

        if (tuple.getValueNode().getNodeType().equals(MAPPING)) {
            return build(paramName, "", parent, position, Parameter.YamlType.MAPPING, (MappingNode) tuple.getValueNode());
        } else if (tuple.getValueNode().getNodeType().equals(SCALAR)) {
            var value = ((ScalarNode) tuple.getValueNode()).getValue();
            if (isNotAKeyword(paramName) && placeHolderResolver.match(value)) {
                //todo what if placeholder in description value? it's not a node
                var resolved = placeHolderResolver.resolve(value);
                if (resolved.isPresent()) {
                    var node = resolved.get();
                    if (SCALAR.equals(node.getNodeType())) {
                        return build(paramName, ((ScalarNode) node).getValue(), parent, position, Parameter.YamlType.SCALAR);
                    } else if (MAPPING.equals(node.getNodeType())) {
                        return build(paramName, "", parent, position, Parameter.YamlType.MAPPING, (MappingNode) node);
                    } else {
                        return build(paramName, "", parent, position, Parameter.YamlType.SEQUENCE, (MappingNode) node);
                    }
                } else {
                    throw new PadmGrammarException(getMessage(MESSAGE_UNKNOWN_TYPE, paramName, value));
                }
            } else if (typeResolvingIsNeeded(paramName, value)) {
                var resolved = resolveTypes(paramName, parent, value.split(OR_TYPE_SPLITTER));
                return resolved;
            }
            return build(paramName, value, parent, position, Parameter.YamlType.SCALAR);
        } else if (tuple.getValueNode().getNodeType().equals(SEQUENCE)) {
            return sequenceParsing(paramName, parent, (SequenceNode) tuple.getValueNode(), position);
        } else {
            System.out.println("something wrong!!");
            return null;
        }
    }

    private Schema resolveTypes(String name, Schema parent, String[] types) {
        if (types.length > 1) {
            if (KeyWord.TYPE.name().equalsIgnoreCase(name)) {
                var oneOf = new Schema("oneOf", "", parent, null, Parameter.YamlType.SEQUENCE);
                var children = Stream.of(types)
                        .map(String::trim)
                        .map(type -> resolveType("", oneOf, type))
//                        .map(Parameter.class::cast)
                        .collect(Collectors.toList());
                oneOf.addChildren(children);
                parent.addChild(oneOf);
                return null;
            } else {
                var param = new Schema(name, "", parent, null, Parameter.YamlType.MAPPING);
                var oneOf = new Schema("oneOf", "", param, null, Parameter.YamlType.SEQUENCE);
                var children = Stream.of(types)
                        .map(String::trim)
                        .map(type -> resolveType("", oneOf, type))
//                        .map(Parameter.class::cast)
                        .collect(Collectors.toList());
                oneOf.addChildren(children);
                param.addChild(oneOf);
                return param;
            }
        } else {
            if (KeyWord.TYPE.name().equalsIgnoreCase(name)) {
                var children = resolveType(parent.getName(), (Schema) parent.getParent(), types[0])
                        .getChildren()
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
                    .map(nodeTuple -> {
                        NodeTuple nodeTuple1 = new NodeTuple(new ScalarNode(
                                Tag.STR, name, ScalarStyle.PLAIN), nodeTuple.getValueNode());
                        return toParameter(nodeTuple1, parent);
                    })
                    .orElseThrow(() -> new PadmGrammarException(getMessage(MESSAGE_UNKNOWN_TYPE, parent, type)));
//            parent.addChildren(schemaParams);
            return schemaParams;
        }
    }

    private Optional<NodeTuple> findCustomType(String type) {
        return typesNode
                .map(NodeTuple::getValueNode)
                .map(MappingNode.class::cast)
                .map(MappingNode::getValue)
                .flatMap(types -> types.stream()
                        .filter(tuple -> getKey(tuple).equalsIgnoreCase(type))
                        .findAny());
    }

    //if paramname == type or paramname is not a keyword(custom type) and value is not a standard type
    private boolean typeResolvingIsNeeded(String paramName, String typeValue) {
        return (isTypeKeyWord(paramName) || isNotAKeyword(paramName)) && isNotAStandardType(typeValue);
    }

    private Schema sequenceParsing(String paramName, Schema parent, SequenceNode node, Position start) {
        var parameter = build(paramName, "", parent, start, Parameter.YamlType.SEQUENCE);

        var children = node.getValue().stream()
                .map(n -> constructParameter(n, parameter))
                .collect(Collectors.toList());
        parameter.addChildren(children);
        return parameter;
    }

    private Schema constructParameter(Node node, Schema parent) {
        var position = getPosition(node);

        if (node instanceof MappingNode) {
            return build(parent, position, Parameter.YamlType.MAPPING, (MappingNode) node);
        } else {
            return build("", ((ScalarNode) node).getValue(), parent, position, Parameter.YamlType.SEQUENCE);
        }
    }

    @Override
    public Node toNode(Schema schema) {
        var tuples = toTuples(schema.getChildren().collect(Collectors.toList()));
        return new MappingNode(Tag.MAP, tuples, FlowStyle.BLOCK);
    }

    private List<NodeTuple> toTuples(List<Parameter> params) {
        return params.stream()
                .map(this::toNodeTuple)
                .collect(Collectors.toList());
    }

    private NodeTuple toNodeTuple(Parameter param) {
        var key = new ScalarNode(Tag.STR, param.getName(), ScalarStyle.PLAIN);
        Node value;
        if (Parameter.YamlType.SCALAR.equals(param.getType())) {
            value = new ScalarNode(Tag.STR, param.getValue() , ScalarStyle.PLAIN);
        } else if (Parameter.YamlType.MAPPING.equals(param.getType())){
            value = new MappingNode(Tag.MAP, toTuples(param.getChildren().collect(Collectors.toList())), FlowStyle.BLOCK);
        } else {
            value = new SequenceNode(Tag.SEQ, toNodes(param.getChildren().collect(Collectors.toList())), FlowStyle.BLOCK);
        }
        return new NodeTuple(key, value);
    }

    private List<Node> toNodes(List<Parameter> params) {
        return params.stream()
                .map(param -> {
                    if (param.getChildren().findAny().isEmpty()) {
                        return new ScalarNode(Tag.STR, param.getValue() , ScalarStyle.PLAIN);
                    } else {
                        return new MappingNode(Tag.MAP, toTuples(param.getChildren().collect(Collectors.toList())), FlowStyle.BLOCK);
                    }
                })
                .collect(Collectors.toList());
    }
}
