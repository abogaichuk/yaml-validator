package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.*;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.SequenceNode;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.snakeyaml.engine.v2.nodes.NodeType.MAPPING;
import static org.snakeyaml.engine.v2.nodes.NodeType.SCALAR;
import static org.snakeyaml.engine.v2.nodes.NodeType.SEQUENCE;

public class YamlMapper {
    private final String TYPES = "Types";
    private Optional<NodeTuple> types;

    public Optional<Definition> toDefinition(Optional<Node> root) {
        root.ifPresent(node -> types = findParameter((MappingNode) node, TYPES));
        return root
            .map(node -> {
                List<Parameter> parameters = toParameters((MappingNode) node);
                return Definition.builder()
                    .parameters(parameters)
                    .resourceType(getScalarValueFrom((MappingNode) node, "ResourceType"))
                    .description(getScalarValueFrom((MappingNode) node, "Description"))
                    .build();
            });
    }

    private List<Parameter> toParameters(MappingNode node) {
        return node.getValue().stream()
            .filter(tuple -> !TYPES.equals(getKey(tuple)))
            .map(this::toParameter)
            .collect(Collectors.toList());
    }

    private Parameter toParameter(NodeTuple tuple) {
        String paramName = getKey(tuple);
        Position start = toPosition(tuple.getKeyNode().getStartMark());
        if (tuple.getValueNode().getNodeType().equals(MAPPING)) {
            return toObjectParameter(paramName, toParameters((MappingNode) tuple.getValueNode()), start);
        } else if (tuple.getValueNode().getNodeType().equals(SCALAR)) {
            return scalarParsing(paramName, tuple, start);
        } else if (tuple.getValueNode().getNodeType().equals(SEQUENCE)) {
            return sequenceParsing(paramName, (SequenceNode) tuple.getValueNode(), start);
        } else {
            System.out.println("something wrong!!");
            return null;
        }
    }

    private Parameter scalarParsing(String paramName, NodeTuple tuple, Position start) {
        String value = getScalarValue(tuple);
        Optional<NodeTuple> customType = findCustomType(value);
        if (customType.isPresent()) {
            return toObjectParameter(paramName, toParameters((MappingNode) customType.get().getValueNode()), start);
        } else {
            return toStringParameter(paramName, value, start);
        }
    }

    private ObjectParameter sequenceParsing(String paramName, SequenceNode node, Position start) {
        List<Parameter> parameters = node.getValue().stream()
            .map(this::constructParameter)
            .collect(Collectors.toList());
        return toObjectParameter(paramName, parameters, start);
    }

    private Parameter constructParameter(Node node) {
        Position position = toPosition(node.getStartMark());
        if (node instanceof MappingNode) {
            return toObjectParameter("", toParameters((MappingNode) node), position);
        } else {
            return toStringParameter("", ((ScalarNode) node).getValue(), position);
        }
    }

    private StringParameter toStringParameter(String name, String value, Position start) {
        return StringParameter.builder()
            .value(value)
            .name(name)
            .position(start)
            .build();
    }

    private ObjectParameter toObjectParameter(String name, List<? extends Parameter> parameters, Position start) {
        return ObjectParameter.builder()
            .name(name)
            .children(parameters)
            .position(start)
            .build();
    }

    private Position toPosition(Optional<Mark> mark) {
        return mark.map(value -> Position.of(value.getLine(), value.getColumn())).orElse(null);
    }

//    private ObjectParameter toObjectParameter(String name, MappingNode node) {
//        List<Parameter> parameters = toParameters(node);
//        return ObjectParameter.builder()
//            .name(name)
//            .children(parameters)
//            .build();
//    }

//    public Optional<ResourceDefinition> toDefinition(Optional<Node> root) {
//        root.ifPresent(node -> types = findParameter((MappingNode) node, TYPES));
//        return root
//            .map(node -> {
//                List<DefinitionParameter<?>> parameters = toList((MappingNode) node);
//                return ResourceDefinition.builder()
//                    .resourceType(getScalarValueFrom((MappingNode) node, "ResourceType"))
//                    .description(getScalarValueFrom((MappingNode) node, "Description"))
//                    .parameters(parameters)
//                    .build();
//            });
//    }
//
//    private List<DefinitionParameter<?>> toList(MappingNode node) {
//        return node.getValue().stream()
//            .filter(tuple -> !TYPES.equals(getKey(tuple)))
//            .map(this::toParam)
//            .collect(Collectors.toList());
//    }
//
//    private DefinitionParameter<?> toParam(NodeTuple pair) {
//        DefinitionParameter.DefinitionParameterBuilder<Object> builder = DefinitionParameter.builder();
//        String paramName = getKey(pair);
//        builder.name(paramName);
//        if (pair.getValueNode().getNodeType().equals(MAPPING)) {
//            MappingNode node = (MappingNode) pair.getValueNode();
//            List<DefinitionParameter<?>> params = toList(node);
//            builder.value(params);
//            builder.type(node.getNodeType().toString());
//        } else if (pair.getValueNode().getNodeType().equals(SCALAR)) {
//            String value = getScalarValue(pair);
//            Optional<NodeTuple> customType = findCustomType(value);
//            if (customType.isPresent()) {
//                List<DefinitionParameter<?>> list = toList((MappingNode) customType.get().getValueNode());
//                builder
//                    .type("MAPPING")
//                    .value(list);
//            } else {
//                builder
//                    .type("SCALAR")
//                    .value(value);
//            }
//        } else if (pair.getValueNode().getNodeType().equals(SEQUENCE)) {
//            SequenceNode node = (SequenceNode) pair.getValueNode();
//            builder.type("SEQUENCE");
//            if (!node.getValue().isEmpty() && node.getValue().get(0) instanceof ScalarNode) {
//                builder.value(getScalarList(node));
//            } else {
//                List<List<DefinitionParameter<?>>> lists = mappingNodeToDefParams(node.getValue());
//                builder.value(lists);
//            }
//        } else {
//            System.out.println("something wrong");
//        }
//        return builder.build();
//    }

    private Optional<NodeTuple> findCustomType(String value) {
        return types
            .map(tuple -> (MappingNode) tuple.getValueNode())
            .flatMap(node -> node.getValue().stream()
                .filter(pair -> value.equals(getKey(pair)))
                .findAny());
    }

//    private List<List<DefinitionParameter<?>>> mappingNodeToDefParams(List<Node> nodes) {
//        return nodes.stream()
//            .map(node -> (MappingNode) node)
//            .map(this::toList)
//            .collect(Collectors.toList());
//    }

    private String getScalarValueFrom(MappingNode node, String param) {
        return findParameter(node, param)
            .map(this::getScalarValue)
            .orElseGet(() -> "");
    }

    private Optional<NodeTuple> findParameter(MappingNode node, String key) {
        return Optional.ofNullable(node)
            .map(MappingNode::getValue)
            .map(List::stream)
            .flatMap(stream -> stream
                .filter(nodeTuple -> getKey(nodeTuple).equals(key))
                .findAny());
    }

    private String getKey(NodeTuple node) {
        return ((ScalarNode)node.getKeyNode()).getValue();
    }

    private String getScalarValue(NodeTuple node) {
        return ((ScalarNode)node.getValueNode()).getValue();
    }

    private List<String> getScalarList(SequenceNode node) {
        return node.getValue().stream()
            .map(n -> (ScalarNode)n)
            .map(ScalarNode::getValue)
            .collect(Collectors.toList());
    }

    private Map<String, Node> extractTypes(Node root) {
        return findParameter((MappingNode) root, "Types")
            .map(NodeTuple::getValueNode)
            .map(n -> (MappingNode) n)
            .map(MappingNode::getValue)
            .map(values -> values.stream()
                .collect(Collectors.toMap(this::getKey, NodeTuple::getValueNode)))
            .orElseGet(Collections::emptyMap);
    }
}
