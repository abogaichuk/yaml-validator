package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.Definition;
import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.Position;
import com.example.yamlvalidator.entity.StringParameter;
import com.example.yamlvalidator.utils.PadmGrammar;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.SequenceNode;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.yamlvalidator.utils.PadmGrammar.*;
import static org.snakeyaml.engine.v2.nodes.NodeType.MAPPING;
import static org.snakeyaml.engine.v2.nodes.NodeType.SCALAR;
import static org.snakeyaml.engine.v2.nodes.NodeType.SEQUENCE;

public class YamlMapper {
    private final String TYPES = "Types";
    private Optional<NodeTuple> typeTuple;

    public Definition toDefinition(Node root) {
//        typeTuple = findParameter((MappingNode) root, TYPES);
        var parameters = toParameters((MappingNode) root);
//        var types = findParameter((MappingNode) root, TYPES)
//                .map(NodeTuple::getValueNode)
//                .filter(node -> node instanceof MappingNode)
//                .map(MappingNode.class::cast)
//                .map(this::toParameters)
//                .orElseGet(Collections::emptyList);
        return Definition.builder()
                .children(parameters)
//                .types(types)
                .name("root")
                .type(Parameter.ParameterType.MAPPING)
                .resourceType(getScalarValueFrom((MappingNode) root, "ResourceType"))
                .description(getScalarValueFrom((MappingNode) root, "Description"))
                .build();
    }

    private List<Parameter> toParameters(MappingNode node) {
        return toParameters(node, "");
    }

    private List<Parameter> toParameters(final MappingNode node, final String parent) {
        return node.getValue().stream()
//            .filter(tuple -> !TYPES.equals(getKey(tuple)))
            .map(n -> toParameter(n, parent))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private Parameter toParameter(final NodeTuple tuple, final String parent) {
        var paramName = getKey(tuple);
        var position = getPosition(tuple);
        var path = parent.isEmpty() ? paramName : parent + "/" + paramName;

//        if (getKeyWord(paramName).isEmpty()) {
//            getStandardType()
//        }

        if (tuple.getValueNode().getNodeType().equals(MAPPING)) {
            return toObjectParameter(paramName, path, Parameter.ParameterType.MAPPING, toParameters((MappingNode) tuple.getValueNode(), path), position);
        } else if (tuple.getValueNode().getNodeType().equals(SCALAR)) {
            var value = getScalarValue(tuple);
//            if (getKeyWord(paramName).isEmpty()) {
//                if (getStandardType(value).isEmpty()) {
//                    Optional<NodeTuple> customType = findCustomType(value);
//                    if (customType.isPresent()) {
//                        List<Parameter> parameters = toParameters((MappingNode) customType.get().getValueNode(), path);
//                        return toObjectParameter(paramName, path, Parameter.ParameterType.MAPPING, parameters, position);
//                    }
//                }
//            }
            return scalarParsing(paramName, value, path, tuple, position);
        } else if (tuple.getValueNode().getNodeType().equals(SEQUENCE)) {
            return sequenceParsing(paramName, path, (SequenceNode) tuple.getValueNode(), position);
        } else {
            System.out.println("something wrong!!");
            return null;
        }
    }

    private Parameter scalarParsing(String paramName, String value, String path, NodeTuple tuple, Position start) {
//        var value = getScalarValue(tuple);
//        var customType = findCustomType(value);

//        validateType(paramName, value, path, start);
        return toStringParameter(paramName, path, Parameter.ParameterType.SCALAR, value, start);
//        if (customType.isPresent()) {
//            return toObjectParameter(paramName, path, Parameter.ParameterType.MAPPING, toParameters((MappingNode) customType.get().getValueNode(), path), start);
//        } else {
//            return toStringParameter(paramName, path, Parameter.ParameterType.SCALAR, value, start);
//        }
    }


    private void validateType(String name, String value, String path, Position start) {
        Optional<String> word = keywords.stream()
                .filter(keyword -> keyword.equalsIgnoreCase(name))
                .findAny();
        if (word.isEmpty()) {
            if (!standardTypes.contains(value)) {
                Optional<NodeTuple> customType = findCustomType(value);
                if (customType.isEmpty()) {
                    throw new IllegalArgumentException("type: " + value + " not found for parameter name: " + name);
                }
            }
        } else if (word.get().equalsIgnoreCase(TYPE_KEY_NAME)) {
            String[] types = value.split("or");
            for (String type : types) {
                if (!standardTypes.contains(type.trim())) {
                    Optional<NodeTuple> customType = findCustomType(type.trim());
                    if (customType.isEmpty()) {
                        throw new IllegalArgumentException("type: " + value + " not found for parameter name: " + name);
                    } else {
                        ObjectParameter objectParameter = toObjectParameter(name, path, Parameter.ParameterType.MAPPING,
                                toParameters((MappingNode) customType.get().getValueNode(), path), start);
                        System.out.println(objectParameter);
                    }
                }
            }
        }
    }

    private List<NodeTuple> findCustomTypes(NodeTuple tuple, String value) {
        String[] types = value.split("or");
        List<NodeTuple> tuples = Stream.of(types)
                .map(this::findCustomType)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        return tuples;
    }

    private ObjectParameter sequenceParsing(String paramName, final String path, SequenceNode node, Position start) {
        var parameters = node.getValue().stream()
            .map(n -> constructParameter(n, path))
            .collect(Collectors.toList());
        return toObjectParameter(paramName, path, Parameter.ParameterType.SEQUENCE, parameters, start);
    }

    private Parameter constructParameter(Node node, String path) {
        var position = node.getStartMark().map(this::toPosition).orElse(null);
        if (node instanceof MappingNode) {
            return toObjectParameter("", path, Parameter.ParameterType.MAPPING, toParameters((MappingNode) node, path), position);
        } else {
            return toStringParameter("", path, Parameter.ParameterType.SCALAR, ((ScalarNode) node).getValue(), position);
        }
    }

    private StringParameter toStringParameter(String name, String path, Parameter.ParameterType type, String value, Position start) {
        return StringParameter.builder()
            .value(value)
            .path(path)
            .type(type)
            .name(name)
            .position(start)
            .build();
    }

    private ObjectParameter toObjectParameter(String name, String path, Parameter.ParameterType type, List<Parameter> parameters, Position start) {
        return ObjectParameter.builder()
            .name(name)
            .path(path)
            .type(type)
            .children(parameters)
            .position(start)
            .build();
    }

    private Position getPosition(final NodeTuple tuple) {
        return tuple.getKeyNode().getStartMark()
            .map(this::toPosition)
            .orElse(null);
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
