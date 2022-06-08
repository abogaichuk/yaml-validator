package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.*;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.nodes.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.snakeyaml.engine.v2.nodes.NodeType.*;

public class ResourceMapper {
    private PlaceHolderResolver placeHolderResolver;

    public ResourceMapper(PlaceHolderResolver placeHolderResolver) {
        this.placeHolderResolver = placeHolderResolver;
    }

    public Resource map(Node node) {
        var resource = new Resource("", "", null, Position.of(1, 1), Param.YamlType.MAPPING);
        var root = (MappingNode) node;
        resource.addChildren(toParameters(root, resource, root).collect(Collectors.toList()));
        return resource;
    }

    public Node map(Resource resource) {
        var tuples = toTuples(resource.getChildren());
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

    private Stream<Resource> toParameters(final MappingNode node, final Resource parent, final MappingNode root) {
        return node.getValue().stream()
                .map(n -> toParameter(n, parent, root))
                .filter(Objects::nonNull);
//                .collect(Collectors.toList());
    }

    private Resource toParameter(final NodeTuple tuple, final Resource parent, final MappingNode root) {
        var paramName = getKey(tuple).toLowerCase();
        var position = getPosition(tuple.getKeyNode());

        if (tuple.getValueNode().getNodeType().equals(MAPPING)) {
            var param = new Resource(paramName, "", parent, position, Param.YamlType.MAPPING);
            param.addChildren(toParameters((MappingNode) tuple.getValueNode(), param, root).collect(Collectors.toList()));
            return param;
        } else if (tuple.getValueNode().getNodeType().equals(SCALAR)) {
            var value = getScalarValue(tuple);
            if (placeHolderResolver.match(value)) {
                var resolved = placeHolderResolver.resolve(value);
                if (resolved.isPresent()) {
                    if (SCALAR.equals(resolved.get().getNodeType())) {
                        return new Resource(paramName, ((ScalarNode) resolved.get()).getValue(), parent, position, Param.YamlType.SCALAR);
                    } else if (MAPPING.equals(resolved.get().getNodeType())) {
                        var param = new Resource(paramName, "", parent, position, Param.YamlType.MAPPING);
                        param.addChildren(toParameters((MappingNode) resolved.get(), param, root).collect(Collectors.toList()));
                        return param;
                    } else {
                        var param = new Resource(paramName, "", parent, position, Param.YamlType.SEQUENCE);
                        param.addChildren(toParameters((MappingNode) resolved.get(), param, root).collect(Collectors.toList()));
                        return param;
                    }
                } else {
                    throw new IllegalArgumentException("placeholder error");
                }
            }
//            value = matchAndReplaceHolders(value);
//            return scalarParsing(paramName, value, parent, position);
            return new Resource(paramName, value, parent, position, Param.YamlType.SCALAR);
        } else if (tuple.getValueNode().getNodeType().equals(SEQUENCE)) {
            return sequenceParsing(paramName, parent, (SequenceNode) tuple.getValueNode(), position, root);
        } else {
            System.out.println("something wrong!!");
            return null;
        }
    }

    private Resource sequenceParsing(String paramName, Resource parent, SequenceNode node, Position start, MappingNode root) {
        var parameter = new Resource(paramName, "", parent, start, Param.YamlType.SEQUENCE);
//        var index = new AtomicInteger(0);

        var children = node.getValue().stream()
                .map(n -> constructParameter(n, parameter, root))
                .map(Param.class::cast)//todo
                .collect(Collectors.toList());
        parameter.addChildren(children);
        return parameter;
    }

    private Resource constructParameter(Node node, Resource parent, MappingNode root) {
        var position = getPosition(node);

        if (node instanceof MappingNode) {
            var p = new Resource("", "", parent, position, Param.YamlType.MAPPING);
            p.addChildren(toParameters((MappingNode) node, p, root).collect(Collectors.toList()));
            return p;
        } else {
            return new Resource("", ((ScalarNode) node).getValue(), parent, position, Param.YamlType.SEQUENCE);
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
