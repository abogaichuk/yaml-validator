package com.example.yamlvalidator.utils;

import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.Position;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.composer.Composer;
import org.snakeyaml.engine.v2.nodes.*;
import org.snakeyaml.engine.v2.parser.ParserImpl;
import org.snakeyaml.engine.v2.scanner.StreamReader;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.yamlvalidator.utils.ValidatorUtils.EMPTY;

public class MappingUtils {

    public static Optional<Node> stringToNode(String yaml) {
        var settings = LoadSettings.builder()
                .setParseComments(true)
                .build();
        var reader = new StreamReader(settings, yaml);
        var parser = new ParserImpl(settings, reader);
        var composer = new Composer(settings, parser);

        return composer.getSingleNode();
    }

    public static Optional<Node> fileToNode(String filename) {
        return Optional.ofNullable(filename)
                .map(fn -> readFile(fn).stream().reduce("", (acc, el) -> acc + el +"\n"))
                .flatMap(MappingUtils::stringToNode);
    }

    private static List<String> readFile(String filename) {
        try {
            return Files.readAllLines(Paths.get(filename));
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

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
//        return parameter.getChildren().findAny().isEmpty()
//                ? new ScalarNode(Tag.STR, parameter.getValue() , ScalarStyle.PLAIN)
//                : new MappingNode(Tag.MAP, toNodes(parameter.getChildren(), MappingUtils::toNodeTuple), FlowStyle.BLOCK);
        if (parameter.getChildren().findAny().isEmpty()) {
            if (EMPTY.equalsIgnoreCase(parameter.getName())) {
                return new ScalarNode(Tag.STR, parameter.getValue() , ScalarStyle.PLAIN);
            } else {
                var tuple = new NodeTuple(new ScalarNode(Tag.STR, parameter.getName(), ScalarStyle.PLAIN),
                        new ScalarNode(Tag.STR, parameter.getValue(), ScalarStyle.PLAIN));
                return new MappingNode(Tag.MAP, List.of(tuple), FlowStyle.BLOCK);
            }
        } else {
            return new MappingNode(Tag.MAP, toNodes(parameter.getChildren(), MappingUtils::toNodeTuple), FlowStyle.BLOCK);
        }
    }
}
