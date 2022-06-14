package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.Position;
import com.example.yamlvalidator.entity.Resource;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.nodes.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.snakeyaml.engine.v2.nodes.NodeType.*;

public class ResourceMapper implements YamlMapper<Resource> {
    private PlaceHolderResolver placeHolderResolver;

    public ResourceMapper(PlaceHolderResolver placeHolderResolver) {
        this.placeHolderResolver = placeHolderResolver;
    }

    @Override
    public Resource map(Node node) {
        return create("", "", null, Position.of(1, 1),
                Parameter.YamlType.MAPPING, (MappingNode) node);
    }

    private Resource create(String name, String value, Resource parent, Position position, Parameter.YamlType type) {
        return create(name, value, parent, position, type, null);
    }

//    @Override
    public Resource create(String name, String value, Resource parent, Position position,
                            Parameter.YamlType type, MappingNode node) {
        var resource = new Resource(name, value, parent, position, type);
        if (node != null) {
            resource.addChildren(toParameters(node, resource).collect(Collectors.toList()));
        }
        return  resource;
    }

    private Stream<Resource> toParameters(final MappingNode node, final Resource parent) {
        return node.getValue().stream()
                .map(n -> toParameter(n, parent))
                .filter(Objects::nonNull);
    }

    private Resource toParameter(final NodeTuple tuple, final Resource parent) {
        var paramName = getKey(tuple).toLowerCase();
        var position = getPosition(tuple.getKeyNode());

        if (tuple.getValueNode().getNodeType().equals(MAPPING)) {
            return create(paramName, "", parent, position, Parameter.YamlType.MAPPING, (MappingNode) tuple.getValueNode());
        } else if (tuple.getValueNode().getNodeType().equals(SCALAR)) {
            var value = ((ScalarNode) tuple.getValueNode()).getValue();
            if (placeHolderResolver.match(value)) {
                var resolved = placeHolderResolver.resolve(value);
                if (resolved.isPresent()) {
                    var node = resolved.get();
                    if (SCALAR.equals(node.getNodeType())) {
                        return create(paramName, ((ScalarNode) node).getValue(), parent, position, Parameter.YamlType.SCALAR);
                    } else {
                        return create(paramName, "", parent, position, Parameter.YamlType.MAPPING, (MappingNode) node);
                    }
                } else {
                    throw new IllegalArgumentException("placeholder error");
                }
            }
            return create(paramName, value, parent, position, Parameter.YamlType.SCALAR);
        } else if (tuple.getValueNode().getNodeType().equals(SEQUENCE)) {
            return sequenceParsing(paramName, parent, (SequenceNode) tuple.getValueNode(), position);
        } else {
            System.out.println("something wrong!!");
            return null;
        }
    }

    private Resource sequenceParsing(String paramName, Resource parent, SequenceNode node, Position position) {
        var parameter = create(paramName, "", parent, position, Parameter.YamlType.SEQUENCE);

        var resources = node.getValue().stream()
                .map(n -> constructParameter(n, parameter));
        parameter.addChildren(resources.collect(Collectors.toList()));

        return parameter;
    }

    private Resource constructParameter(Node node, Resource parent) {
        var position = getPosition(node);

        if (node instanceof MappingNode) {
            return create("", "", parent, position, Parameter.YamlType.MAPPING, (MappingNode) node);
        } else {
            return create("", ((ScalarNode) node).getValue(), parent, position, Parameter.YamlType.SEQUENCE);
        }
    }
}
