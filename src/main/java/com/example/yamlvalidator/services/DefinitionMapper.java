package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.*;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.nodes.*;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.valueOf;
import static org.snakeyaml.engine.v2.nodes.NodeType.*;

public class DefinitionMapper {
    private PlaceHolderResolver placeHolderResolver;

    public DefinitionMapper(PlaceHolderResolver placeHolderResolver) {
        this.placeHolderResolver = placeHolderResolver;
    }

    public Definition map(Node root, boolean resolvePlaceholders) {
        var children = toParameters((MappingNode) root, "").collect(Collectors.toList());
        return new Definition(children);
    }

    private Stream<Parameter> toParameters(final MappingNode node, final String parentPath) {
        return node.getValue().stream()
                .map(n -> toParameter(n, parentPath))
                .filter(Objects::nonNull);
    }

    private Parameter toParameter(final NodeTuple tuple, final String parentPath) {
        var paramName = getKey(tuple).toLowerCase();
        var position = getPosition(tuple.getKeyNode());
        var path = parentPath.isEmpty() ? paramName : parentPath + "/" + paramName;

        if (tuple.getValueNode().getNodeType().equals(MAPPING)) {
            var children = toParameters((MappingNode) tuple.getValueNode(), path).collect(Collectors.toList());
            return Parameter.of(paramName, path, children, position);
        } else if (tuple.getValueNode().getNodeType().equals(SCALAR)) {
            var value = getScalarValue(tuple);
            if (placeHolderResolver.match(value)) {
                var o = placeHolderResolver.resolve(value);
                System.out.println(o);
            }
            return Parameter.of(paramName, path, value, position);
        } else if (tuple.getValueNode().getNodeType().equals(SEQUENCE)) {
            return sequenceParsing(paramName, path, (SequenceNode) tuple.getValueNode(), position);
        } else {
            System.out.println("something wrong!!");
            return null;
        }
    }

    private Parameter sequenceParsing(String paramName, String path, SequenceNode node, Position position) {
        var index = new AtomicInteger(0);

        var children = node.getValue().stream()
                .map(n -> constructParameter(n, path, index.getAndIncrement()))
                .map(Parameter.class::cast)//todo
                .collect(Collectors.toList());
        return Parameter.of(paramName, path, children, position);
    }

    private Parameter constructParameter(Node node, String path, int index) {
        var position = getPosition(node);

        if (node instanceof MappingNode) {
            var children = toParameters((MappingNode) node, path).collect(Collectors.toList());
            return Parameter.of(valueOf(index), path, children, position);
        } else {
            return Parameter.of(valueOf(index), path, ((ScalarNode) node).getValue(), position);
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
