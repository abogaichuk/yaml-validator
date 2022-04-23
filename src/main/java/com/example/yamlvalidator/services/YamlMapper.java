package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.Definition;
import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.Position;
import com.example.yamlvalidator.entity.StringParameter;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.SequenceNode;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.snakeyaml.engine.v2.nodes.NodeType.MAPPING;
import static org.snakeyaml.engine.v2.nodes.NodeType.SCALAR;
import static org.snakeyaml.engine.v2.nodes.NodeType.SEQUENCE;

public class YamlMapper {
    private final String TYPES = "Types";
    private Optional<NodeTuple> typeTuple;

    public Definition toDefinition(Node root) {
        typeTuple = findParameter((MappingNode) root, TYPES);
        var parameters = toParameters((MappingNode) root);
        return Definition.builder()
            .parameters(parameters)
            .resourceType(getScalarValueFrom((MappingNode) root, "ResourceType"))
            .description(getScalarValueFrom((MappingNode) root, "Description"))
            .build();
    }

    private List<Parameter> toParameters(MappingNode node) {
        return node.getValue().stream()
            .filter(tuple -> !TYPES.equals(getKey(tuple)))
            .map(this::toParameter)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private Parameter toParameter(NodeTuple tuple) {
        var paramName = getKey(tuple);
        var position = tuple.getKeyNode().getStartMark()
            .map(this::toPosition).orElse(null);
        if (tuple.getValueNode().getNodeType().equals(MAPPING)) {
            return toObjectParameter(paramName, toParameters((MappingNode) tuple.getValueNode()), position);
        } else if (tuple.getValueNode().getNodeType().equals(SCALAR)) {
            return scalarParsing(paramName, tuple, position);
        } else if (tuple.getValueNode().getNodeType().equals(SEQUENCE)) {
            return sequenceParsing(paramName, (SequenceNode) tuple.getValueNode(), position);
        } else {
            System.out.println("something wrong!!");
            return null;
        }
    }

    private Parameter scalarParsing(String paramName, NodeTuple tuple, Position start) {
        var value = getScalarValue(tuple);
        var customType = findCustomType(value);
        if (customType.isPresent()) {
            return toObjectParameter(paramName, toParameters((MappingNode) customType.get().getValueNode()), start);
        } else {
            return toStringParameter(paramName, value, start);
        }
    }

    private ObjectParameter sequenceParsing(String paramName, SequenceNode node, Position start) {
        var parameters = node.getValue().stream()
            .map(this::constructParameter)
            .collect(Collectors.toList());
        return toObjectParameter(paramName, parameters, start);
    }

    private Parameter constructParameter(Node node) {
        var position = node.getStartMark().map(this::toPosition).orElse(null);
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

    private Position toPosition(Mark mark) {
        return Position.of(mark.getLine(), mark.getColumn());
    }

    private Optional<NodeTuple> findCustomType(String value) {
        return typeTuple
            .map(tuple -> (MappingNode) tuple.getValueNode())
            .flatMap(node -> node.getValue().stream()
                .filter(pair -> value.equals(getKey(pair)))
                .findAny());
    }

    private String getScalarValueFrom(MappingNode node, String param) {
        return findParameter(node, param)
            .map(this::getScalarValue)
            .orElseGet(() -> "");
    }

    private Optional<NodeTuple> findParameter(MappingNode node, String key) {
        return node.getValue().stream()
            .filter(nodeTuple -> getKey(nodeTuple).equals(key))
            .findAny();
    }

    private String getKey(NodeTuple node) {
        return ((ScalarNode)node.getKeyNode()).getValue();
    }

    private String getScalarValue(NodeTuple node) {
        return ((ScalarNode)node.getValueNode()).getValue();
    }
}
