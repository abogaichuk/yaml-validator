package com.example.yamlvalidator.mappers;

import com.example.yamlvalidator.entity.Builder;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.Position;
import com.example.yamlvalidator.entity.Resource;
import com.example.yamlvalidator.errors.PadmGrammarException;
import com.example.yamlvalidator.utils.MappingUtils;
import lombok.Getter;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.nodes.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.yamlvalidator.grammar.KeyWord.TYPE;
import static com.example.yamlvalidator.grammar.KeyWord.TYPES;
import static com.example.yamlvalidator.utils.MappingUtils.getName;
import static com.example.yamlvalidator.utils.MappingUtils.getPosition;
import static com.example.yamlvalidator.utils.ValidatorUtils.EMPTY;
import static org.snakeyaml.engine.v2.nodes.NodeType.*;

public abstract class AbstractMapper {

    @Getter
    private final Builder builder;

    public AbstractMapper() {
        builder = Resource.ResourceBuilder.builder();
    }

    public AbstractMapper(Builder builder) {
        this.builder = builder;
    }

    public abstract Parameter scalarParsing(String name, String value, Parameter parent, Position position);

    public Optional<Parameter> mapToParam(String yaml) {
        return MappingUtils.stringToNode(yaml)
                .map(node -> buildEmpty((MappingNode) node));
    }

    protected Parameter buildEmpty(MappingNode node) {
        return build(EMPTY, EMPTY, null, Position.of(0, 0), Parameter.YamlType.MAPPING, node);
    }

    protected List<Parameter> toParameters(final CollectionNode<?> node, final Parameter parent) {
        return node.getValue().stream()
                .map(o -> {
                    if (o instanceof NodeTuple) {
                        var tuple = (NodeTuple) o;
                        if (TYPES.lowerCase().equals(getName(tuple.getKeyNode())))
                            return null; //skip types section
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

    protected Parameter toParameter(final Node keyNode, final Node valueNode, final Parameter parent) {
        var name = getName(keyNode);
        var position = getPosition(keyNode);

        if (MAPPING.equals(valueNode.getNodeType())) {
            return build(name, EMPTY, parent, position, Parameter.YamlType.MAPPING, valueNode);
        } else if (SCALAR.equals(valueNode.getNodeType())) {
            return scalarParsing(name, ((ScalarNode) valueNode).getValue(), parent, position);
        } else if (SEQUENCE.equals(valueNode.getNodeType())) {
            return build(name, EMPTY, parent, position, Parameter.YamlType.SEQUENCE, valueNode);
        } else {
            throw new PadmGrammarException("unknown node type: " + valueNode.getNodeType());
        }
    }

    protected Parameter build(String name, String value, Parameter parent, Position position, Parameter.YamlType type) {
        return getBuilder().name(name).value(value).parent(parent).position(position).yamlType(type).build();
    }

    protected Parameter build(String name, String value, Parameter parent, Position position, Parameter.YamlType type, Node node) {
        var resource = build(name, value, parent, position, type);
        buildChildren(node, resource).ifPresent(resource::addChildren);
        return resource;
    }

    protected Optional<List<Parameter>> buildChildren(Node node, Parameter parent) {
        return Optional.ofNullable(node)
                .map(n -> {
                    if (n instanceof CollectionNode) {
                        return toParameters((CollectionNode<?>) node, parent);
                    } else {
                        return List.of(build(TYPE.name().toLowerCase(), ((ScalarNode) node).getValue(),
                                parent, null, Parameter.YamlType.SCALAR));
                    }
                });
    }

    public Node mapToNode(Parameter root) {
        var tuples = toNodes(root.getChildren(), this::toNodeTuple);
        return new MappingNode(Tag.MAP, tuples, FlowStyle.BLOCK);
    }

    private NodeTuple toNodeTuple(Parameter param) {
        var key = new ScalarNode(Tag.STR, param.getName(), ScalarStyle.PLAIN);

        Node value;
        if (Parameter.YamlType.SCALAR.equals(param.getType())) {
            value = new ScalarNode(Tag.STR, param.getValue() , ScalarStyle.PLAIN);
        } else if (Parameter.YamlType.MAPPING.equals(param.getType())) {
            var nodes = toNodes(param.getChildren(), this::toNodeTuple);
            value = new MappingNode(Tag.MAP, nodes, FlowStyle.BLOCK);
        } else {
            var nodes = toNodes(param.getChildren(), this::toNode);
            value = new SequenceNode(Tag.SEQ, nodes, FlowStyle.BLOCK);
        }
        return new NodeTuple(key, value);
    }

    private <N> List<N> toNodes(Stream<Parameter> params, Function<Parameter, N> transformation) {
        return params
                .filter(Objects::nonNull)
                .map(transformation)
                .collect(Collectors.toList());
    }

    private Node toNode(Parameter parameter) {
        if (parameter.getChildren().findAny().isEmpty()) {
            if (EMPTY.equalsIgnoreCase(parameter.getName())) {
                return new ScalarNode(Tag.STR, parameter.getValue() , ScalarStyle.PLAIN);
            } else {
                var tuple = new NodeTuple(new ScalarNode(Tag.STR, parameter.getName(), ScalarStyle.PLAIN),
                        new ScalarNode(Tag.STR, parameter.getValue(), ScalarStyle.PLAIN));
                return new MappingNode(Tag.MAP, List.of(tuple), FlowStyle.BLOCK);
            }
        } else {
            return new MappingNode(Tag.MAP, toNodes(parameter.getChildren(), this::toNodeTuple), FlowStyle.BLOCK);
        }
    }
}
