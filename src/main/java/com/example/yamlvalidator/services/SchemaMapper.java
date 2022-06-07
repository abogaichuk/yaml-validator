package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.*;
import com.example.yamlvalidator.grammar.KeyWord;
import com.example.yamlvalidator.utils.ValidatorUtils;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.nodes.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.yamlvalidator.utils.ValidatorUtils.*;
import static java.lang.String.valueOf;
import static org.snakeyaml.engine.v2.nodes.NodeType.*;

public class SchemaMapper {
    private PlaceHolderResolver placeHolderResolver;

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
                    throw new IllegalArgumentException("placeholder error");
                }
            } else if (typeResolvingIsNeeded(paramName, value)) {
                var types = value.split(OR_TYPE_SPLITTER);
                if (types.length > 1) {
                    var param = new SchemaParam(paramName, "", parent, position, Param.YamlType.MAPPING);
                    var optionals = resolveTypes(types, root);

                    var oneOf = new SchemaParam("oneOf", "", param, position, Param.YamlType.SEQUENCE);
                    var params = new ArrayList<Param>();
                    for (var node : optionals) {
                        if (node.isPresent()) {
                            params.add(toParameter(node.get(), oneOf, root));
                        } else {
                            throw new IllegalArgumentException("placeholder error");
                        }
                    }
                    oneOf.addChildren(params);
                    param.addChildren(Collections.singletonList(oneOf));
                    return param;
                } else {
                    var node = resolveType(types[0], root);
                    if (node.isPresent()) {
                        var param = new SchemaParam(paramName, "", parent, position, Param.YamlType.MAPPING);
                        param.addChildren(Collections.singletonList(toParameter(node.get(), param, root)));
                        return param;
                    } else {
                        throw new IllegalArgumentException("placeholder error: " + paramName);
                    }
                }
            }
//            value = matchAndReplaceHolders(value);
//            return scalarParsing(paramName, value, parent, position);
            return new SchemaParam(paramName, value, parent, position, Param.YamlType.SCALAR);
        } else if (tuple.getValueNode().getNodeType().equals(SEQUENCE)) {
            return sequenceParsing(paramName, parent, (SequenceNode) tuple.getValueNode(), position, root);
        } else {
            System.out.println("something wrong!!");
            return null;
        }
    }

    private Optional<NodeTuple> resolveType(String type, MappingNode root) {
        return root.getValue().stream()
                .filter(nodeTuple -> getKey(nodeTuple).equalsIgnoreCase(type))
                .findAny();
    }

    private List<Optional<NodeTuple>> resolveTypes(String[] types, MappingNode root) {
        return Stream.of(types)
                .map(String::trim)
                .map(splittedType -> resolveType(splittedType, root))
                .collect(Collectors.toList());
    }

    private boolean typeResolvingIsNeeded(String paramName, String typeValue) {
        return isNotAKeyword(paramName) && isNotAStandardType(typeValue);
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
