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

    default Node map(T parameter) {
        var tuples = toTuples(parameter.getChildren());
        return new MappingNode(Tag.MAP, tuples, FlowStyle.BLOCK);
    }

    default Position getPosition(final Node node) {
        return node.getStartMark()
                .map(mark -> Position.of(mark.getLine(), mark.getColumn()))
                .orElse(null);
    }

    default String getKey(NodeTuple node) {
        return ((ScalarNode) node.getKeyNode()).getValue();
    }

    private List<NodeTuple> toTuples(Stream<Parameter> params) {
        return params
                .map(this::toNodeTuple)
                .collect(Collectors.toList());
    }

    private NodeTuple toNodeTuple(Parameter param) {
        var key = new ScalarNode(Tag.STR, param.getName(), ScalarStyle.PLAIN);
        Node value;
        if (Parameter.YamlType.SCALAR.equals(param.getType())) {
            value = new ScalarNode(Tag.STR, param.getValue() , ScalarStyle.PLAIN);
        } else if (Parameter.YamlType.MAPPING.equals(param.getType())){
            value = new MappingNode(Tag.MAP, toTuples(param.getChildren()), FlowStyle.BLOCK);
        } else {
            value = new SequenceNode(Tag.SEQ, toNodes(param.getChildren()), FlowStyle.BLOCK);
        }
        return new NodeTuple(key, value);
    }

//    private <N> List<N> toNodes(Stream<Parameter> params, Function<Parameter, N> transformation) {
//        return params
//                .map(transformation)
//                .collect(Collectors.toList());
//    }

    private List<Node> toNodes(Stream<Parameter> params) {
        return params
                .map(this::toNode)
                .collect(Collectors.toList());
    }
//    Function<Parameter, Node> toNode = parameter -> {
//        if (parameter.getChildren().findAny().isEmpty()) {
//            return new ScalarNode(Tag.STR, parameter.getValue() , ScalarStyle.PLAIN);
//        } else {
//            return new MappingNode(Tag.MAP, toTuples(parameter.getChildren()), FlowStyle.BLOCK);
//        }
//    };

    private Node toNode(Parameter parameter) {
        if (parameter.getChildren().findAny().isEmpty()) {
            return new ScalarNode(Tag.STR, parameter.getValue() , ScalarStyle.PLAIN);
        } else {
            return new MappingNode(Tag.MAP, toTuples(parameter.getChildren()), FlowStyle.BLOCK);
        }
    }
}
