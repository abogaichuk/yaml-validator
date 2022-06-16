package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.Position;
import com.example.yamlvalidator.entity.Resource;
import com.example.yamlvalidator.errors.PadmGrammarException;
import org.snakeyaml.engine.v2.nodes.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.example.yamlvalidator.utils.MappingUtils.getName;
import static com.example.yamlvalidator.utils.MappingUtils.getPosition;
import static com.example.yamlvalidator.utils.ValidatorUtils.EMPTY;
import static org.snakeyaml.engine.v2.nodes.NodeType.*;

public class ResourceMapper {
    private final PlaceHolderResolver placeHolderResolver;
    private final MappingNode rootNode;

    public ResourceMapper(MappingNode node) {
        Objects.requireNonNull(node);
        this.rootNode = node;
        this.placeHolderResolver = new PlaceHolderResolver(node);
    }

    public Resource map() {
        var root = Resource.builder()
                .position(Position.of(1, 1))
                .yamlType(Parameter.YamlType.MAPPING)
                .build();
        root.addChildren(toParameters(rootNode, root));
        return root;
    }

    private List<Resource> toParameters(final CollectionNode<?> node, final Resource parent) {
        return node.getValue().stream()
                .map(o -> {
                    if (o instanceof NodeTuple) {
                        var tuple = (NodeTuple) o;
                        return toParameter(tuple.getKeyNode(), tuple.getValueNode(), parent);
                    } else if (o instanceof Node) {
                        return toParameter(null, (Node) o, parent);
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Resource toParameter(final Node keyNode, final Node valueNode, final Resource parent) {
        var builder = Resource.builder()
                .name(getName(keyNode))
                .position(getPosition(keyNode))
                .parent(parent);

        if (valueNode.getNodeType().equals(MAPPING)) {
            return build(EMPTY, Parameter.YamlType.MAPPING, builder, (MappingNode) valueNode);
        } else if (valueNode.getNodeType().equals(SCALAR)) {
            return scalarParsing(((ScalarNode) valueNode).getValue(), builder);
        } else if (valueNode.getNodeType().equals(SEQUENCE)) {
            return build(EMPTY, Parameter.YamlType.SEQUENCE, builder, (SequenceNode) valueNode);
        } else {
            throw new PadmGrammarException("unknown node type: " + valueNode.getNodeType());
        }
    }

    private Resource scalarParsing(String value, Resource.ResourceBuilder builder) {
        if (placeHolderResolver.match(value)) {
            return placeHolderResolver.resolve(value)
                    .map(node -> {
                        if (SCALAR.equals(node.getNodeType())) {
                            return build(((ScalarNode) node).getValue(), Parameter.YamlType.SCALAR, builder, null);
                        } else {
                            return build(EMPTY, Parameter.YamlType.MAPPING, builder, (MappingNode) node);
                        }
                    })
                    .orElseThrow(() -> new PadmGrammarException("placeholder error for value: " + value));
        }
        return build(value, Parameter.YamlType.SCALAR, builder, null);
    }

    private Resource build(String value, Parameter.YamlType type,
                           Resource.ResourceBuilder builder, CollectionNode<?> node) {
        var resource = builder.value(value)
                .yamlType(type)
                .build();
        if (node != null) {
            resource.addChildren(toParameters(node, resource));
        }
        return resource;
    }
}
