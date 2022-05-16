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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.example.yamlvalidator.utils.ValidatorUtils.*;
import static java.lang.String.*;
import static org.snakeyaml.engine.v2.nodes.NodeType.MAPPING;
import static org.snakeyaml.engine.v2.nodes.NodeType.SCALAR;
import static org.snakeyaml.engine.v2.nodes.NodeType.SEQUENCE;

public class YamlMapper {
    public Definition toDefinition(Node root) {
        var definition = new Definition("root", Parameter.ParameterType.MAPPING, null, null,
                getScalarValueFrom((MappingNode) root, "ResourceType"), null);
        var parameters = toParameters((MappingNode) root, definition);
        definition.addChildren(parameters);
        return definition;
    }

    private List<Parameter> toParameters(MappingNode node) {
        return toParameters(node, null);
    }

    private List<Parameter> toParameters(final MappingNode node, final ObjectParameter parent) {
        return node.getValue().stream()
            .map(n -> toParameter(n, parent))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private Parameter toParameter(final NodeTuple tuple, final ObjectParameter parent) {
        var paramName = getKey(tuple).toLowerCase();
        var position = getPosition(tuple.getKeyNode());

        if (tuple.getValueNode().getNodeType().equals(MAPPING)) {
            ObjectParameter parameter = toObjectParameter(paramName, parent, Parameter.ParameterType.MAPPING, position);
            parameter.addChildren(toParameters((MappingNode) tuple.getValueNode(), parameter));
            return parameter;
        } else if (tuple.getValueNode().getNodeType().equals(SCALAR)) {
            var value = getScalarValue(tuple);
            value = matchAndReplaceHolders(value);
            return scalarParsing(paramName, value, parent, position);
        } else if (tuple.getValueNode().getNodeType().equals(SEQUENCE)) {
            return sequenceParsing(paramName, parent, (SequenceNode) tuple.getValueNode(), position);
        } else {
            System.out.println("something wrong!!");
            return null;
        }
    }

    private StringParameter scalarParsing(String paramName, String value, ObjectParameter parent, Position start) {
        return toStringParameter(paramName, parent, value, start);
    }

    private ObjectParameter sequenceParsing(String paramName, ObjectParameter parent, SequenceNode node, Position start) {
        ObjectParameter parameter = toObjectParameter(paramName, parent, Parameter.ParameterType.SEQUENCE, start);
        AtomicInteger index = new AtomicInteger(0);

        var children = node.getValue().stream()
            .map(n -> constructParameter(n, parameter, index.getAndIncrement()))
            .collect(Collectors.toList());
        parameter.addChildren(children);
        return parameter;
    }

    private Parameter constructParameter(Node node, ObjectParameter parent, int index) {
        var position = getPosition(node);

        if (node instanceof MappingNode) {
            ObjectParameter parameter = toObjectParameter(valueOf(index), parent, Parameter.ParameterType.MAPPING, position);
            parameter.addChildren(toParameters((MappingNode) node, parameter));
            return parameter;
        } else {
            return toStringParameter(valueOf(index), parent, ((ScalarNode) node).getValue(), position);
        }
    }

    private StringParameter toStringParameter(String name, ObjectParameter parent, String value, Position start) {
        return new StringParameter(name, Parameter.ParameterType.SCALAR, parent, start, value);
    }

    private ObjectParameter toObjectParameter(String name, ObjectParameter parent, Parameter.ParameterType type, Position start) {
        return new ObjectParameter(name, type, parent, start);
    }

    private Position getPosition(final Node node) {
        return node.getStartMark()
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
