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

import java.util.*;
import java.util.stream.Collectors;

import static com.example.yamlvalidator.utils.ValidatorUtils.*;
import static org.snakeyaml.engine.v2.nodes.NodeType.MAPPING;
import static org.snakeyaml.engine.v2.nodes.NodeType.SCALAR;
import static org.snakeyaml.engine.v2.nodes.NodeType.SEQUENCE;

public class YamlMapper {
    public Definition toDefinition(Node root) {
//        var definition = Definition.builder()
////                .types(types)
//                .name("root")
//                .type(Parameter.ParameterType.MAPPING)
//                .resourceType(getScalarValueFrom((MappingNode) root, "ResourceType"))
//                .description(getScalarValueFrom((MappingNode) root, "Description"))
//                .build();
        var definition = new Definition("root", Parameter.ParameterType.MAPPING, null, null, null,
                getScalarValueFrom((MappingNode) root, "ResourceType"), null);
        var parameters = toParameters((MappingNode) root, definition);
        definition.setChildren(parameters);
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

    private StringParameter scalarParsing(String paramName, String value, ObjectParameter parent, NodeTuple tuple, Position start) {
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
        return new StringParameter(name, type, parent, start, value);
//        return StringParameter.builder()
//            .value(value)
////            .path(path)
//            .parent(parent)
//            .type(type)
//            .name(name)
//            .position(start)
//            .build();
    }

    private ObjectParameter toObjectParameter(String name, ObjectParameter parent, Parameter.ParameterType type, Position start) {
        return new ObjectParameter(name, type, parent, start, null);
//        return ObjectParameter.builder()
//                .name(name)
////            .path(path)
//                .parent(parent)
//                .type(type)
////                .children(parameters)
//                .position(start)
//                .build();
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
