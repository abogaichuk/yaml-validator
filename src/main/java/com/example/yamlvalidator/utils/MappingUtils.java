package com.example.yamlvalidator.utils;

import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.Position;
import com.example.yamlvalidator.entity.Resource;
import com.example.yamlvalidator.errors.PadmGrammarException;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.composer.Composer;
import org.snakeyaml.engine.v2.nodes.*;
import org.snakeyaml.engine.v2.parser.ParserImpl;
import org.snakeyaml.engine.v2.scanner.StreamReader;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.yamlvalidator.utils.ValidatorUtils.EMPTY;
import static org.snakeyaml.engine.v2.nodes.NodeType.*;

public class MappingUtils {

    public static Optional<Node> stringToNode(String yaml) {
        var settings = LoadSettings.builder().build();
        var composer = new Composer(settings, new ParserImpl(settings, new StreamReader(settings, yaml)));

        return composer.getSingleNode();
    }

//    public static Node map(Parameter root) {
//        var tuples = toNodes(root.getChildren(), MappingUtils::toNodeTuple);
//        return new MappingNode(Tag.MAP, tuples, FlowStyle.BLOCK);
//    }

//    public static Parameter map(String yaml) {
//        return MappingUtils.stringToNode(yaml)
//                .map(node -> build(EMPTY, Parameter.YamlType.MAPPING, Resource.builder(), (MappingNode) node))
//                .orElseThrow(() -> new PadmGrammarException("wrong!!"));
//    }

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

//    private static NodeTuple toNodeTuple(Parameter param) {
//        var key = new ScalarNode(Tag.STR, param.getName(), ScalarStyle.PLAIN);
//
//        Node value;
//        if (Parameter.YamlType.SCALAR.equals(param.getType())) {
//            value = new ScalarNode(Tag.STR, param.getValue() , ScalarStyle.PLAIN);
//        } else if (Parameter.YamlType.MAPPING.equals(param.getType())) {
//            var nodes = toNodes(param.getChildren(), MappingUtils::toNodeTuple);
//            value = new MappingNode(Tag.MAP, nodes, FlowStyle.BLOCK);
//        } else {
//            var nodes = toNodes(param.getChildren(), MappingUtils::toNode);
//            value = new SequenceNode(Tag.SEQ, nodes, FlowStyle.BLOCK);
//        }
//        return new NodeTuple(key, value);
//    }
//
//    private static <N> List<N> toNodes(Stream<Parameter> params, Function<Parameter, N> transformation) {
//        return params
//                .map(transformation)
//                .collect(Collectors.toList());
//    }
//
//    private static Node toNode(Parameter parameter) {
////        return parameter.getChildren().findAny().isEmpty()
////                ? new ScalarNode(Tag.STR, parameter.getValue() , ScalarStyle.PLAIN)
////                : new MappingNode(Tag.MAP, toNodes(parameter.getChildren(), MappingUtils::toNodeTuple), FlowStyle.BLOCK);
//        if (parameter.getChildren().findAny().isEmpty()) {
//            if (EMPTY.equalsIgnoreCase(parameter.getName())) {
//                return new ScalarNode(Tag.STR, parameter.getValue() , ScalarStyle.PLAIN);
//            } else {
//                var tuple = new NodeTuple(new ScalarNode(Tag.STR, parameter.getName(), ScalarStyle.PLAIN),
//                        new ScalarNode(Tag.STR, parameter.getValue(), ScalarStyle.PLAIN));
//                return new MappingNode(Tag.MAP, List.of(tuple), FlowStyle.BLOCK);
//            }
//        } else {
//            return new MappingNode(Tag.MAP, toNodes(parameter.getChildren(), MappingUtils::toNodeTuple), FlowStyle.BLOCK);
//        }
//    }

//    private static List<Parameter> toParameters(final CollectionNode<?> node, final Parameter parent) {
//        return node.getValue().stream()
//                .map(o -> {
//                    if (o instanceof NodeTuple) {
//                        var tuple = (NodeTuple) o;
//                        return toParameter(tuple.getKeyNode(), tuple.getValueNode(), parent);
//                    } else if (o instanceof Node) {
//                        return toParameter(null, (Node) o, parent);
//                    } else {
//                        return null;
//                    }
//                })
//                .filter(Objects::nonNull)
//                .collect(Collectors.toList());
//    }
//
//    private static Parameter toParameter(final Node keyNode, final Node valueNode, final Parameter parent) {
//        var builder = Resource.builder()
//                .name(getName(keyNode))
//                .position(getPosition(keyNode))
//                .parent(parent);
//
//        if (MAPPING.equals(valueNode.getNodeType())) {
//            return build(EMPTY, Parameter.YamlType.MAPPING, builder, (MappingNode) valueNode);
//        } else if (SCALAR.equals(valueNode.getNodeType())) {
//            return build(((ScalarNode) valueNode).getValue(), Parameter.YamlType.SCALAR, builder, null);
////            return scalarParsing(((ScalarNode) valueNode).getValue(), builder);
//        } else if (SEQUENCE.equals(valueNode.getNodeType())) {
//            return build(EMPTY, Parameter.YamlType.SEQUENCE, builder, (SequenceNode) valueNode);
//        } else {
//            throw new PadmGrammarException("unknown node type: " + valueNode.getNodeType());
//        }
//    }
//
//    private static Parameter build(String value, Parameter.YamlType type,
//                                   Resource.ResourceBuilder builder, CollectionNode<?> node) {
//        var resource = builder.value(value)
//                .yamlType(type)
//                .build();
//        if (node != null) {
//            resource.addChildren(toParameters(node, resource));
//        }
//        return resource;
//    }
}
