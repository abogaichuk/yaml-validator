package com.example.yamlvalidator.utils;

import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.Position;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.nodes.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.yamlvalidator.utils.ValidatorUtils.EMPTY;

public class MappingUtils {

    public static Node map(Parameter root) {
        var tuples = toNodes(root.getChildren(), MappingUtils::toNodeTuple);
        return new MappingNode(Tag.MAP, tuples, FlowStyle.BLOCK);
    }

    public static Position getPosition(final Node node) {
        return Optional.ofNullable(node)
                .flatMap(Node::getStartMark)
                .map(mark -> Position.of(mark.getLine(), mark.getColumn()))
                .orElse(null);
    }

    public static String getName(final Node keyNode) {
        return Optional.ofNullable(keyNode)
                .map(ScalarNode.class::cast)
                .map(ScalarNode::getValue)
                .map(String::toLowerCase)
                .orElse(EMPTY);
    }

    private static NodeTuple toNodeTuple(Parameter param) {
        var key = new ScalarNode(Tag.STR, param.getName(), ScalarStyle.PLAIN);

        Node value;
        if (Parameter.YamlType.SCALAR.equals(param.getType())) {
            value = new ScalarNode(Tag.STR, param.getValue() , ScalarStyle.PLAIN);
        } else if (Parameter.YamlType.MAPPING.equals(param.getType())) {
            var nodes = toNodes(param.getChildren(), MappingUtils::toNodeTuple);
            value = new MappingNode(Tag.MAP, nodes, FlowStyle.BLOCK);
        } else {
            var nodes = toNodes(param.getChildren(), MappingUtils::toNode);
            value = new SequenceNode(Tag.SEQ, nodes, FlowStyle.BLOCK);
        }
        return new NodeTuple(key, value);
    }

    private static <N> List<N> toNodes(Stream<Parameter> params, Function<Parameter, N> transformation) {
        return params
                .map(transformation)
                .collect(Collectors.toList());
    }

    private static Node toNode(Parameter parameter) {
        return parameter.getChildren().findAny().isEmpty()
                ? new ScalarNode(Tag.STR, parameter.getValue() , ScalarStyle.PLAIN)
                : new MappingNode(Tag.MAP, toNodes(parameter.getChildren(), MappingUtils::toNodeTuple), FlowStyle.BLOCK);
    }
}
