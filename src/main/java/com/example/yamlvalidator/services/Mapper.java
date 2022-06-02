package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.*;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.nodes.*;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.valueOf;
import static org.snakeyaml.engine.v2.nodes.NodeType.*;

public class Mapper {

    public Schema mapToSchema(Node root) {
        var definition = new Schema("", "", null, Position.of(1, 1));
        definition.addChildren(toParameters((MappingNode) root, definition).collect(Collectors.toList()));
        return definition;
    }

    public Resource mapToResources(Node root) {
        var resource = new Resource("", "", null, Position.of(1, 1));
        resource.addChildren(toParameters((MappingNode) root, null)
                .collect(Collectors.toList()));
        return resource;
//        return toParameters((MappingNode) root, null)
//                .map(Resource.class::cast)
//                .collect(Collectors.toList());
    }

    private Stream<Param> toParameters(final MappingNode node, final Param parent) {
        return node.getValue().stream()
                .map(n -> toParameter(n, parent))
                .filter(Objects::nonNull);
//                .collect(Collectors.toList());
    }

    private Param toParameter(final NodeTuple tuple, final Param parent) {
        var paramName = getKey(tuple).toLowerCase();
        var position = getPosition(tuple.getKeyNode());

        if (tuple.getValueNode().getNodeType().equals(MAPPING)) {
            Param param = parameterFactory(paramName, "", parent, position);
            param.addChildren(toParameters((MappingNode) tuple.getValueNode(), param).collect(Collectors.toList()));
            return param;
        } else if (tuple.getValueNode().getNodeType().equals(SCALAR)) {
            var value = getScalarValue(tuple);
//            value = matchAndReplaceHolders(value);
//            return scalarParsing(paramName, value, parent, position);
            return parameterFactory(paramName, value, parent, position);
        } else if (tuple.getValueNode().getNodeType().equals(SEQUENCE)) {
            return sequenceParsing(paramName, parent, (SequenceNode) tuple.getValueNode(), position);
        } else {
            System.out.println("something wrong!!");
            return null;
        }
    }

    private Param parameterFactory(String name, String value, Param parent, Position position) {
        return parent instanceof SchemaParam
                ? new SchemaParam(name, value, parent, position)
                : new Resource(name, value, parent, position);
    }

    private Param sequenceParsing(String paramName, Param parent, SequenceNode node, Position start) {
        Param parameter = parameterFactory(paramName, "", parent, start);
        var index = new AtomicInteger(0);

        var children = node.getValue().stream()
                .map(n -> constructParameter(n, parameter, index.getAndIncrement()))
                .collect(Collectors.toList());
        parameter.addChildren(children);
        return parameter;
    }

    private Param constructParameter(Node node, Param parent, int index) {
        var position = getPosition(node);

        if (node instanceof MappingNode) {
            Param p = parameterFactory(valueOf(index), "", parent, position);
            p.addChildren(toParameters((MappingNode) node, p).collect(Collectors.toList()));
            return p;
        } else {
            return parameterFactory(valueOf(index), ((ScalarNode) node).getValue(), parent, position);
        }
    }

    private Position getPosition(final Node node) {
        return node.getStartMark()
                .map(this::toPosition)
                .orElse(null);
    }

    private Position toPosition(Mark mark) {
        return Position.of(mark.getLine(), mark.getColumn());
    }

    private String getKey(NodeTuple node) {
        return ((ScalarNode) node.getKeyNode()).getValue();
    }

    private String getScalarValue(NodeTuple node) {
        return ((ScalarNode) node.getValueNode()).getValue();
    }
}
