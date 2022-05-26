package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.*;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.nodes.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.lang.String.valueOf;
import static org.snakeyaml.engine.v2.nodes.NodeType.*;

public class Mapper {
    public Schema map(Node root) {
        var definition = new Schema(null, null, null, Position.of(1, 1));
        var parameters = toParameters((MappingNode) root, definition);
        definition.addChildren(parameters);
        return definition;
    }

    private List<Param> toParameters(MappingNode node) {
        return toParameters(node, null);
    }

    private List<Param> toParameters(final MappingNode node, final Param parent) {
        return node.getValue().stream()
                .map(n -> toParameter(n, parent))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Param toParameter(final NodeTuple tuple, final Param parent) {
        var paramName = getKey(tuple).toLowerCase();
        var position = getPosition(tuple.getKeyNode());

        if (tuple.getValueNode().getNodeType().equals(MAPPING)) {
//            Param param = toParam(paramName, "", parent, position, Parameter.ParameterType.MAPPING);
            Param param = new Param(paramName, "", parent, position);
            param.addChildren(toParameters((MappingNode) tuple.getValueNode(), param));
            return param;
        } else if (tuple.getValueNode().getNodeType().equals(SCALAR)) {
            var value = getScalarValue(tuple);
//            value = matchAndReplaceHolders(value);
//            return scalarParsing(paramName, value, parent, position);
            return new Param(paramName, value, parent, position);
        } else if (tuple.getValueNode().getNodeType().equals(SEQUENCE)) {
            return sequenceParsing(paramName, parent, (SequenceNode) tuple.getValueNode(), position);
        } else {
            System.out.println("something wrong!!");
            return null;
        }
    }

    private Param sequenceParsing(String paramName, Param parent, SequenceNode node, Position start) {
//        Param parameter = toObjectParameter(paramName, parent, Parameter.ParameterType.SEQUENCE, start);
        Param parameter = new Param(paramName, "", parent, start);
        AtomicInteger index = new AtomicInteger(0);

        var children = node.getValue().stream()
                .map(n -> constructParameter(n, parameter, index.getAndIncrement()))
                .collect(Collectors.toList());
        parameter.addChildren(children);
        return parameter;
    }

    private Param constructParameter(Node node, Param parent, int index) {
        var position = getPosition(node);

        if (node instanceof MappingNode) {
//            Param p = toObjectParameter(valueOf(index), parent, Parameter.ParameterType.MAPPING, position);
            Param p = new Param(valueOf(index), "", parent, position);
            p.addChildren(toParameters((MappingNode) node, p));
            return p;
        } else {
            return new Param(valueOf(index), ((ScalarNode) node).getValue(), parent, position);
//            return toStringParameter(valueOf(index), parent, ((ScalarNode) node).getValue(), position);
        }
    }

//    private Param toParam(String name, String value, Param parent, Position start, Parameter.ParameterType type) {
//        return new Param(name, parent, start);
//    }

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
        return ((ScalarNode) node.getKeyNode()).getValue();
    }

    private String getScalarValue(NodeTuple node) {
        return ((ScalarNode) node.getValueNode()).getValue();
    }
}
