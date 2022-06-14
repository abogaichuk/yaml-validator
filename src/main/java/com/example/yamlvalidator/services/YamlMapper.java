package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.Position;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.nodes.*;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface YamlMapper<T extends Parameter> {
    T map(Node node);

    default Position getPosition(final Node node) {
        return node.getStartMark()
                .map(mark -> Position.of(mark.getLine(), mark.getColumn()))
                .orElse(null);
    }

    default String getKey(NodeTuple node) {
        return ((ScalarNode) node.getKeyNode()).getValue();
    }

    default Node map(T parameter) {
        var tuples = toNodes(parameter.getChildren(), this::toNodeTuple);
        return new MappingNode(Tag.MAP, tuples, FlowStyle.BLOCK);
    }

    private NodeTuple toNodeTuple(Parameter param) {
        var key = new ScalarNode(Tag.STR, param.getName(), ScalarStyle.PLAIN);

        Node value;
        if (Parameter.YamlType.SCALAR.equals(param.getType())) {
            value = new ScalarNode(Tag.STR, param.getValue() , ScalarStyle.PLAIN);
        } else if (Parameter.YamlType.MAPPING.equals(param.getType())) {
            var nodes = toNodes(param.getChildren(), this::toNodeTuple);
            value = new MappingNode(Tag.MAP, nodes, FlowStyle.BLOCK);
        } else {
            var nodes = toNodes(param.getChildren(), this::toNode);
            value = new SequenceNode(Tag.SEQ, nodes, FlowStyle.BLOCK);
        }
        return new NodeTuple(key, value);
    }

    private <N> List<N> toNodes(Stream<Parameter> params, Function<Parameter, N> transformation) {
        return params
                .map(transformation)
                .collect(Collectors.toList());
    }

    private Node toNode(Parameter parameter) {
        return parameter.getChildren().findAny().isEmpty()
                ? new ScalarNode(Tag.STR, parameter.getValue() , ScalarStyle.PLAIN)
                : new MappingNode(Tag.MAP, toNodes(parameter.getChildren(), this::toNodeTuple), FlowStyle.BLOCK);
    }
}
