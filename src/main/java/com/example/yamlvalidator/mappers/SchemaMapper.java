package com.example.yamlvalidator.mappers;

import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.Position;
import com.example.yamlvalidator.entity.Schema;
import com.example.yamlvalidator.errors.PadmGrammarException;
import com.example.yamlvalidator.utils.ValidatorUtils;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.nodes.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.yamlvalidator.grammar.KeyWord.ONEOF;
import static com.example.yamlvalidator.grammar.KeyWord.TYPE;
import static com.example.yamlvalidator.utils.MappingUtils.getName;
import static com.example.yamlvalidator.utils.MessagesUtils.MESSAGE_UNKNOWN_TYPE;
import static com.example.yamlvalidator.utils.MessagesUtils.getMessage;
import static com.example.yamlvalidator.utils.ValidatorUtils.*;

public class SchemaMapper extends PlaceholderMapper {

    public SchemaMapper() {
        super(Schema.SchemaBuilder.builder());
    }

    @Override
    protected Parameter scalarParsing(String name, String value, Parameter parent, Position position) {
//        if (isNotAKeyword(name) && match(value)) {
        if (match(name)) {
            var types = value.split(OR_TYPE_SPLITTER);
            if (isTheTypeNode(name)) {
                if (types.length > 1) {
                    setOneOf(parent, types);
                } else {
                    parent.addChildren(resolveChildren(name, types[0], parent));
                }
                return null; //return null because we want to delete type section and update its parent
            } else {
                return types.length > 1 ? resolveOneOf(name, parent, position, types) : resolveParam(name, types[0], (Schema) parent, position);
            }
        }
        return super.scalarParsing(name, value, parent, position);
    }

    private boolean match(String paramName) {
        return isTypeKeyWord(paramName) || (isNotEmpty(paramName) && isNotAKeyword(paramName));
    }

    private Parameter resolveOneOf(String name, Parameter parent, Position position, String[] types) {
        var param = build(name, EMPTY, parent, position, Parameter.YamlType.MAPPING);
        setOneOf(param, types);
        return param;
    }

    private boolean isTheTypeNode(String name) {
        return TYPE.lowerCase().equals(name);
    }

    private Parameter resolveParam(String name, String type, Schema parent, Position position) {
        return getStandardType(type)
                .map(tuple -> build(name, EMPTY, parent, position, Parameter.YamlType.MAPPING, tuple.getValueNode()))
                .orElseGet(() -> getCustomType(type)
                        .map(tuple -> build(name, EMPTY, parent, position, Parameter.YamlType.MAPPING, tuple.getValueNode()))
                        .orElseThrow(() -> new PadmGrammarException(getMessage(MESSAGE_UNKNOWN_TYPE, parent, type))));
    }

    private List<Parameter> resolveChildren(String name, String type, Parameter parent) {
        return getStandardType(type)
                .map(tuple -> List.of(build(name, type, parent, null, Parameter.YamlType.SCALAR)))
                .orElseGet(() -> getCustomType(type)
                        .flatMap(tuple -> buildChildren(tuple.getValueNode(), parent))
                        .orElseThrow(() -> new PadmGrammarException(getMessage(MESSAGE_UNKNOWN_TYPE, parent, type))));
    }

    private void setOneOf(Parameter parent, String[] types) {
        var oneOf = build(ONEOF.lowerCase(), EMPTY, parent, null, Parameter.YamlType.SEQUENCE);
        var children = Stream.of(types)
                .map(String::trim)
                .map(type -> resolveParam(EMPTY, type, (Schema) oneOf, null))
                .collect(Collectors.toList());
        oneOf.addChildren(children);
        parent.addChildren(List.of(oneOf));
    }

    private Optional<NodeTuple> getStandardType(String type) {
        return Optional.ofNullable(type)
                .filter(ValidatorUtils::isStandardType)
                .map(t -> new NodeTuple(
                        new ScalarNode(Tag.STR, TYPE.name().toLowerCase(), ScalarStyle.PLAIN),
                        new ScalarNode(Tag.STR, t, ScalarStyle.PLAIN)));
    }

    @Override
    protected Parameter build(String name, String value, Parameter parent, Position position, Parameter.YamlType type, Node node) {
        if (parent != null) {
            ((Schema) parent).deleteIfPresent(name);
        }
        return super.build(name, value, parent, position, type, node);
    }

    private Optional<NodeTuple> getCustomType(String type) {
        return nodeTypes
                .map(NodeTuple::getValueNode)
                .map(MappingNode.class::cast)
                .map(MappingNode::getValue)
                .flatMap(types -> types.stream()
                        .filter(tuple -> getName(tuple.getKeyNode()).equalsIgnoreCase(type))
                        .findAny());
    }
}
