package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.Param;
import com.example.yamlvalidator.entity.Position;
import com.example.yamlvalidator.entity.Schema;
import com.example.yamlvalidator.entity.SchemaParam;
import com.example.yamlvalidator.errors.PadmGrammarException;
import com.example.yamlvalidator.grammar.KeyWord;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.nodes.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.yamlvalidator.utils.MessagesUtils.MESSAGE_UNKNOWN_TYPE;
import static com.example.yamlvalidator.utils.MessagesUtils.getMessage;
import static com.example.yamlvalidator.utils.ValidatorUtils.*;
import static org.snakeyaml.engine.v2.nodes.NodeType.*;

public class SchemaMapper {
    private final PlaceHolderResolver placeHolderResolver;

    public SchemaMapper(PlaceHolderResolver placeHolderResolver) {
        this.placeHolderResolver = placeHolderResolver;
    }

    public Schema map(Node node) {
        var definition = new Schema("", "", null, Position.of(1, 1), Param.YamlType.MAPPING);
        var root = (MappingNode) node;
        definition.addChildren(toParameters(root, definition, root).collect(Collectors.toList()));
        return definition;
    }

    public Node map(Schema schema) {
        var tuples = toTuples(schema.getChildren());
        return new MappingNode(new Tag("tag:yaml.org,2002:map"), tuples, FlowStyle.BLOCK);
    }

    private List<NodeTuple> toTuples(List<Param> params) {
        return params.stream()
                .map(this::toNodeTuple)
                .collect(Collectors.toList());
    }

    private NodeTuple toNodeTuple(Param param) {
        var key = new ScalarNode(new Tag("tag:yaml.org,2002:str"), param.getName(), ScalarStyle.PLAIN);
        Node value;
        if (Param.YamlType.SCALAR.equals(param.getYamlType())) {
            value = new ScalarNode(new Tag("tag:yaml.org,2002:str"), param.getValue() , ScalarStyle.PLAIN);
        } else if (Param.YamlType.MAPPING.equals(param.getYamlType())){
            value = new MappingNode(new Tag("tag:yaml.org,2002:map"), toTuples(param.getChildren()), FlowStyle.BLOCK);
        } else {
            value = new SequenceNode(new Tag("tag:yaml.org,2002:seq"), toNodes(param.getChildren()), FlowStyle.BLOCK);
        }
        return new NodeTuple(key, value);
    }

    private List<Node> toNodes(List<Param> params) {
        return params.stream()
                .map(param -> {
                    if (param.getChildren().isEmpty()) {
                        return new ScalarNode(new Tag("tag:yaml.org,2002:str"), param.getValue() , ScalarStyle.PLAIN);
                    } else {
                        return new MappingNode(new Tag("tag:yaml.org,2002:map"), toTuples(param.getChildren()), FlowStyle.BLOCK);
                    }
                })
                .collect(Collectors.toList());
    }

    private Stream<SchemaParam> toParameters(final MappingNode node, final SchemaParam parent, final MappingNode root) {
        return node.getValue().stream()
                .map(n -> toParameter(n, parent, root))
                .filter(Objects::nonNull);
//                .collect(Collectors.toList());
    }

    private SchemaParam toParameter(final NodeTuple tuple, final SchemaParam parent, final MappingNode root) {
        var paramName = getKey(tuple).toLowerCase();
        var position = getPosition(tuple.getKeyNode());

        if (tuple.getValueNode().getNodeType().equals(MAPPING)) {
            var param = new SchemaParam(paramName, "", parent, position, Param.YamlType.MAPPING);
            param.addChildren(toParameters((MappingNode) tuple.getValueNode(), param, root).collect(Collectors.toList()));
            return param;
        } else if (tuple.getValueNode().getNodeType().equals(SCALAR)) {
            var value = getScalarValue(tuple);
            if (isNotAKeyword(paramName) && placeHolderResolver.match(value)) {
                //todo what if placeholder in description value? it's not a node
                var resolved = placeHolderResolver.resolve(value);
                if (resolved.isPresent()) {
                    if (SCALAR.equals(resolved.get().getNodeType())) {
                        return new SchemaParam(paramName, ((ScalarNode) resolved.get()).getValue(), parent, position, Param.YamlType.SCALAR);
                    } else if (MAPPING.equals(resolved.get().getNodeType())) {
                        var param = new SchemaParam(paramName, "", parent, position, Param.YamlType.MAPPING);
                        param.addChildren(toParameters((MappingNode) resolved.get(), param, root).collect(Collectors.toList()));
                        return param;
                    } else {
                        var param = new SchemaParam(paramName, "", parent, position, Param.YamlType.SEQUENCE);
                        param.addChildren(toParameters((MappingNode) resolved.get(), param, root).collect(Collectors.toList()));
                        return param;
                    }
                } else {
                    throw new PadmGrammarException(getMessage(MESSAGE_UNKNOWN_TYPE, paramName, value));
                }
            } else if (typeResolvingIsNeeded(paramName, value)) {
                var resolved = resolveTypes(paramName, parent, value.split(OR_TYPE_SPLITTER), root);
                return resolved;
            }
            return new SchemaParam(paramName, value, parent, position, Param.YamlType.SCALAR);
        } else if (tuple.getValueNode().getNodeType().equals(SEQUENCE)) {
            return sequenceParsing(paramName, parent, (SequenceNode) tuple.getValueNode(), position, root);
        } else {
            System.out.println("something wrong!!");
            return null;
        }
    }

    private SchemaParam resolveTypes(String name, SchemaParam parent, String[] types, MappingNode root) {
        if (types.length > 1) {
            if (KeyWord.TYPE.name().equalsIgnoreCase(name)) {
                var oneOf = new SchemaParam("oneOf", "", parent, null, Param.YamlType.SEQUENCE);
                var children = Stream.of(types)
                        .map(String::trim)
                        .map(type -> resolveType("", oneOf, type, root))
                        .map(Param.class::cast)
                        .collect(Collectors.toList());
                oneOf.addChildren(children);
                parent.addChild(oneOf);
                return null;
            } else {
                var param = new SchemaParam(name, "", parent, null, Param.YamlType.MAPPING);
                var oneOf = new SchemaParam("oneOf", "", param, null, Param.YamlType.SEQUENCE);
                var children = Stream.of(types)
                        .map(String::trim)
                        .map(type -> resolveType("", oneOf, type, root))
                        .map(Param.class::cast)
                        .collect(Collectors.toList());
                oneOf.addChildren(children);
                param.addChild(oneOf);
                return param;
            }
        } else {
            if (KeyWord.TYPE.name().equalsIgnoreCase(name)) {
                var children = resolveType(parent.getName(), (SchemaParam) parent.getParent(), types[0], root)
                        .getChildren();
                parent.addChildren(children);
                return null;
            } else {
//                var param = new SchemaParam(name, "", parent, null, Param.YamlType.MAPPING);
//                param.addChild(resolveType(name, param, types[0], root));
//                return param;
                SchemaParam param = resolveType(name, parent, types[0], root);
                return param;
            }
        }
    }

    private SchemaParam resolveType(String name, SchemaParam parent, String type, MappingNode root) {
        if (isStandardType(type)) {
            return new SchemaParam("", type, parent, null, Param.YamlType.SCALAR);
        } else {
            Optional<NodeTuple> customType = findCustomType(type, root);
            SchemaParam schemaParams = customType
                    .map(nodeTuple -> {
                        NodeTuple nodeTuple1 = new NodeTuple(new ScalarNode(
                                new Tag("tag:yaml.org,2002:str"), name, ScalarStyle.PLAIN), nodeTuple.getValueNode());
                        return toParameter(nodeTuple1, parent, root);
                    })
                    .orElseThrow(() -> new PadmGrammarException(getMessage(MESSAGE_UNKNOWN_TYPE, parent, type)));
//            parent.addChildren(schemaParams);
            return schemaParams;
        }
    }

//    private void resolveTypes(SchemaParam param, String[] types, MappingNode root) {
//        var oneOf = new SchemaParam("oneOf", "", param, null, Param.YamlType.SEQUENCE);
//
//        var children = Stream.of(types)
//                .map(String::trim)
//                .map(splittedType -> {
//                    if (isStandardType(splittedType)) {
//                        return new SchemaParam("", splittedType, oneOf, null, Param.YamlType.SCALAR);
//                    } else {
//                        return findCustomType(splittedType, root)
//                                .map(nodeTuple -> toParameter(nodeTuple, oneOf, root))
//                                .orElseThrow(() -> new PadmGrammarException(
//                                        messageProvider.getMessage(MESSAGE_UNKNOWN_TYPE, param, splittedType)));
//                    }
//                }).map(Param.class::cast)
//                .collect(Collectors.toList());
//
//        oneOf.addChildren(children);
//        param.addChildren(Collections.singletonList(oneOf));
//    }

//    private Param resolveType(String type, MappingNode root, SchemaParam parent, Pos) {
//        if (isStandardType(type)) {
//            return new SchemaParam(paramName, type, parent, position, Param.YamlType.SCALAR);
//        }
//    }

    private Optional<NodeTuple> findCustomType(String type, MappingNode root) {
        return root.getValue().stream()
                .filter(nodeTuple -> getKey(nodeTuple).equalsIgnoreCase(type))
                .findAny();
    }


//    private List<Param> resolveCustomTypes(String[] types, MappingNode root, SchemaParam oneOf) {
//        var params = new ArrayList<Param>();
//        var optionals = Stream.of(types)
//                .map(String::trim)
//                .map(splittedType -> resolveCustomType(splittedType, root))
//                .collect(Collectors.toList());
//        for (var node : optionals) {
//            if (node.isPresent()) {
//                params.add(toParameter(node.get(), oneOf, root));
//            } else {
//                throw new PadmGrammarException(messageProvider.getMessage(MESSAGE_UNKNOWN_TYPE, param, value));
//            }
//        }
//        return params;
//    }
//
//    private List<Optional<NodeTuple>> resolveCustomTypes(String[] types, MappingNode root) {
//        return Stream.of(types)
//                .map(String::trim)
//                .map(splittedType -> resolveCustomType(splittedType, root))
//                .collect(Collectors.toList());
//    }

    //if paramname == type or paramname is not a keyword(custom type) and value is not a standard type
    private boolean typeResolvingIsNeeded(String paramName, String typeValue) {
        return (isTypeKeyWord(paramName) || isNotAKeyword(paramName)) && isNotAStandardType(typeValue);
    }

    private SchemaParam sequenceParsing(String paramName, SchemaParam parent, SequenceNode node, Position start, MappingNode root) {
        var parameter = new SchemaParam(paramName, "", parent, start, Param.YamlType.SEQUENCE);
//        var index = new AtomicInteger(0);

        var children = node.getValue().stream()
                .map(n -> constructParameter(n, parameter, root))
                .map(Param.class::cast)//todo
                .collect(Collectors.toList());
        parameter.addChildren(children);
        return parameter;
    }

    private SchemaParam constructParameter(Node node, SchemaParam parent, MappingNode root) {
        var position = getPosition(node);

        if (node instanceof MappingNode) {
            var p = new SchemaParam("", "", parent, position, Param.YamlType.MAPPING);
            p.addChildren(toParameters((MappingNode) node, p, root).collect(Collectors.toList()));
            return p;
        } else {
            return new SchemaParam("", ((ScalarNode) node).getValue(), parent, position, Param.YamlType.SEQUENCE);
        }
    }

    private Position getPosition(final Node node) {
        return node.getStartMark()
                .map(this::toPosition)
                .orElse(null);
    }

    private Position toPosition(Mark mark) {
        return Position.of(mark.getLine(), mark.getColumn());
    }

    private String getKey(NodeTuple node) {
        return ((ScalarNode) node.getKeyNode()).getValue();
    }

    private String getScalarValue(NodeTuple node) {
        return ((ScalarNode) node.getValueNode()).getValue();
    }
}
