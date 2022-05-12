package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.Definition;
import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.Position;
import com.example.yamlvalidator.entity.StringParameter;
import com.example.yamlvalidator.utils.PadmGrammar;
import com.example.yamlvalidator.utils.ValidatorUtils;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.SequenceNode;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.yamlvalidator.utils.PadmGrammar.*;
import static com.example.yamlvalidator.utils.ValidatorUtils.*;
import static org.snakeyaml.engine.v2.nodes.NodeType.MAPPING;
import static org.snakeyaml.engine.v2.nodes.NodeType.SCALAR;
import static org.snakeyaml.engine.v2.nodes.NodeType.SEQUENCE;

public class YamlMapper {
    private final String TYPES = "Types";

    public Definition toDefinition(Node root) {
        var definition = Definition.builder()
//                .types(types)
                .name("root")
                .type(Parameter.ParameterType.MAPPING)
                .resourceType(getScalarValueFrom((MappingNode) root, "ResourceType"))
                .description(getScalarValueFrom((MappingNode) root, "Description"))
                .build();
        var parameters = toParameters((MappingNode) root, definition);
        definition.setChildren(parameters);
//        var types = findParameter((MappingNode) root, TYPES)
//                .map(NodeTuple::getValueNode)
//                .filter(node -> node instanceof MappingNode)
//                .map(MappingNode.class::cast)
//                .map(this::toParameters)
//                .orElseGet(Collections::emptyList);
        return definition;
    }

    private List<Parameter> toParameters(MappingNode node) {
        return toParameters(node, null);
    }

    private List<Parameter> toParameters(final MappingNode node, final ObjectParameter parent) {
        return node.getValue().stream()
//            .filter(tuple -> !TYPES.equals(getKey(tuple)))
            .map(n -> toParameter(n, parent))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private Parameter toParameter(final NodeTuple tuple, final ObjectParameter parent) {
        var paramName = getKey(tuple).toLowerCase();
        var position = getPosition(tuple);
        var path = parent == null ? paramName : parent.getPath() + "/" + paramName;

//        if (getKeyWord(paramName).isEmpty()) {
//            getStandardType()
//        }

        if (tuple.getValueNode().getNodeType().equals(MAPPING)) {
            ObjectParameter parameter = toObjectParameter(paramName, parent, Parameter.ParameterType.MAPPING, position);
            parameter.setChildren(toParameters((MappingNode) tuple.getValueNode(), parameter));
            return parameter;
        } else if (tuple.getValueNode().getNodeType().equals(SCALAR)) {
            var value = getScalarValue(tuple);
            value = matchAndReplaceHolders(value);
//            if (getKeyWord(paramName).isEmpty()) {
//                if (getStandardType(value).isEmpty()) {
//                    Optional<NodeTuple> customType = findCustomType(value);
//                    if (customType.isPresent()) {
//                        List<Parameter> parameters = toParameters((MappingNode) customType.get().getValueNode(), path);
//                        return toObjectParameter(paramName, path, Parameter.ParameterType.MAPPING, parameters, position);
//                    }
//                }
//            }
            return scalarParsing(paramName, value, parent, tuple, position);
        } else if (tuple.getValueNode().getNodeType().equals(SEQUENCE)) {
            return sequenceParsing(paramName, parent, (SequenceNode) tuple.getValueNode(), position);
        } else {
            System.out.println("something wrong!!");
            return null;
        }
    }

    private Parameter scalarParsing(String paramName, String value, ObjectParameter parent, NodeTuple tuple, Position start) {
//        var value = getScalarValue(tuple);
//        var customType = findCustomType(value);

//        validateType(paramName, value, path, start);
        return toStringParameter(paramName, parent, Parameter.ParameterType.SCALAR, value, start);
//        if (customType.isPresent()) {
//            return toObjectParameter(paramName, path, Parameter.ParameterType.MAPPING, toParameters((MappingNode) customType.get().getValueNode(), path), start);
//        } else {
//            return toStringParameter(paramName, path, Parameter.ParameterType.SCALAR, value, start);
//        }
    }


//    private void validateType(String name, String value, String path, Position start) {
//        Optional<String> word = keywords.stream()
//                .filter(keyword -> keyword.equalsIgnoreCase(name))
//                .findAny();
//        if (word.isEmpty()) {
//            if (!standardTypes.contains(value)) {
//                Optional<NodeTuple> customType = findCustomType(value);
//                if (customType.isEmpty()) {
//                    throw new IllegalArgumentException("type: " + value + " not found for parameter name: " + name);
//                }
//            }
//        } else if (word.get().equalsIgnoreCase(TYPE_KEY_NAME)) {
//            String[] types = value.split("or");
//            for (String type : types) {
//                if (!standardTypes.contains(type.trim())) {
//                    Optional<NodeTuple> customType = findCustomType(type.trim());
//                    if (customType.isEmpty()) {
//                        throw new IllegalArgumentException("type: " + value + " not found for parameter name: " + name);
//                    } else {
//                        ObjectParameter objectParameter = toObjectParameter(name, path, Parameter.ParameterType.MAPPING,
//                                toParameters((MappingNode) customType.get().getValueNode(), path), start);
//                        System.out.println(objectParameter);
//                    }
//                }
//            }
//        }
//    }

    private ObjectParameter sequenceParsing(String paramName, final ObjectParameter parent, SequenceNode node, Position start) {
        ObjectParameter parameter = toObjectParameter(paramName, parent, Parameter.ParameterType.SEQUENCE, start);
        var children = node.getValue().stream()
            .map(n -> constructParameter(n, parameter))
            .collect(Collectors.toList());
        parameter.setChildren(children);
        return parameter;
    }

    private Parameter constructParameter(Node node, ObjectParameter parent) {
        var position = node.getStartMark().map(this::toPosition).orElse(null);
        if (node instanceof MappingNode) {
            ObjectParameter parameter = toObjectParameter("", parent, Parameter.ParameterType.MAPPING, position);
            parameter.setChildren(toParameters((MappingNode) node, parameter));
            return parameter;
        } else {
            return toStringParameter("", parent, Parameter.ParameterType.SCALAR, ((ScalarNode) node).getValue(), position);
        }
    }

    private StringParameter toStringParameter(String name, ObjectParameter parent, Parameter.ParameterType type, String value, Position start) {
        return StringParameter.builder()
            .value(value)
//            .path(path)
            .parent(parent)
            .type(type)
            .name(name)
            .position(start)
            .build();
    }

    private ObjectParameter toObjectParameter(String name, ObjectParameter parent, Parameter.ParameterType type, Position start) {
        return ObjectParameter.builder()
                .name(name)
//            .path(path)
                .parent(parent)
                .type(type)
//                .children(parameters)
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
