package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.Position;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.ScalarNode;

public interface YamlMapper<T> {
    T toParameter(Node node);
    Node toNode(T t);

    default Position getPosition(final Node node) {
        return node.getStartMark()
                .map(mark -> Position.of(mark.getLine(), mark.getColumn()))
                .orElse(null);
    }

    default String getKey(NodeTuple node) {
        return ((ScalarNode) node.getKeyNode()).getValue();
    }
}
