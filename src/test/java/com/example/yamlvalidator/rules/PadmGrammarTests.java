package com.example.yamlvalidator.rules;

import com.example.yamlvalidator.entity.*;
import com.example.yamlvalidator.grammar.RuleService;
import com.example.yamlvalidator.services.MessageProvider;
import com.example.yamlvalidator.utils.ValidatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public class PadmGrammarTests {

    RuleService rules = new RuleService();

    @Test
    public void when_correct_returnValid() {
        var schema = createSchema(getNumberParams());
        var result = schema.validate(rules, null);
        System.out.println(result);
//        assertTrue(StandardType.NUMBER.validate(createSchema(getNumberParams())).isValid());
//        assertTrue(StandardType.DATETIME.validate(createSchema(getDatetimeParams())).isValid());
//        assertTrue(StandardType.STRING.validate(createSchema(getStringParams())).isValid());
//        assertTrue(StandardType.BOOLEAN.validate(createSchema(getBooleanParams())).isValid());
    }

//    @Test
//    public void when_incorrectNumber_returnInvalid() {
//        assertFalse(StandardType.NUMBER.validate(createParent(getMinLessThanMaxParams())).isValid());
//        assertFalse(StandardType.NUMBER.validate(createParent(getDefaultLessThanMinParams())).isValid());
//        assertFalse(StandardType.NUMBER.validate(createParent(getDefaultMoreThanMaxParams())).isValid());
//        assertFalse(StandardType.NUMBER.validate(createParent(getListDoesNotContainDefaultParams())).isValid());
//
//        var result = StandardType.NUMBER.validate(createParent(minMaxAndDefaultAreNans()));
//        assertEquals(3, result.getReasons().size());
//    }
//
//    @Test
//    public void when_incorrectDatetime_returnInvalid() {
//        assertEquals(3, StandardType.DATETIME.validate(createParent(incorrectPatternParams())).getReasons().size());
//
//        assertFalse(StandardType.DATETIME.validate(createParent(defaultAfterBefore())).isValid());
//        assertFalse(StandardType.DATETIME.validate(createParent(defaultBeforeAfter())).isValid());
//        assertFalse(StandardType.DATETIME.validate(createParent(afterAfterBefore())).isValid());
//    }
//
//    @Test
//    public void when_incorrectBoolean_returnInvalid() {
//        assertFalse(StandardType.BOOLEAN.validate(createParent(defaultIsNotABoolean())).isValid());
//    }
//
//    @Test
//    public void when_incorrectString_returnInvalid() {
//        assertFalse(StandardType.STRING.validate(createParent(defaultIsNotInList())).isValid());
//    }

//    @Test
//    public void when_incorrectOneOf_returnInvalid() {
//        assertFalse(StandardType.OBJECT.validate(createParent()).isValid());
//    }

    private Schema createSchema(Map<String, List<String>> params) {
        var parent = new Schema("", "", null, Position.of(0, 1), Param.YamlType.MAPPING);
        parent.addChildren(createChildren(parent, params));
        return parent;
    }

    private List<Param> createChildren(Param parent, Map<String, List<String>> params) {
        var index = new AtomicInteger(1);
        return params.entrySet().stream()
                .map(entry -> createChild(parent, entry.getKey(), entry.getValue(), index.getAndIncrement()))
                .collect(Collectors.toList());
    }

    private Param createChild(Param parent, String name, List<String> values, int column) {
        if (values.size() > 1) {
            var param = new SchemaParam(name, "", parent, Position.of(0, column), Param.YamlType.MAPPING);
            for (var v : values) {
                param.addChild(createChild(param, "", List.of(v), 0));
            }
            return param;
        } else {
            return new SchemaParam(name, values.get(0), parent, Position.of(0, column), Param.YamlType.SCALAR);
        }
    }

//    private Param createObjectParam(Param parent, String name, List<String> values, int column) {
//        var parameter = new SchemaParam(name, Parameter.ParameterType.MAPPING, parent, Position.of(0, column));
//        var columnIndex = new AtomicInteger(column);
//        var nameIndex = new AtomicInteger(1);
//        List<Parameter> children = values.stream()
//                .map(value -> createStringParam(String.valueOf(nameIndex.getAndIncrement()), value, columnIndex.getAndIncrement(), parameter))
//                .collect(Collectors.toList());
//        parameter.addChildren(children);
//        return parameter;
//    }
//
//    private StringParameter createStringParam(String name, String value, int column, ObjectParameter parent) {
//        return new StringParameter(name, Parameter.ParameterType.SCALAR, parent, Position.of(0, column), value);
//    }

//    private Map<String, Map<String, List<String>>>

    private Map<String, List<String>> getNumberParams() {
        return Map.of(
                "type", List.of("number"),
                "default", List.of("dasd"),
                "min", List.of("1"),
                "max", List.of("10"),
                "list", List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
                );
    }

    private Map<String, List<String>> getDatetimeParams() {
        return Map.of(
                "type", List.of("datetime"),
                "default", List.of("2000-01-01 00:01"),
                "before", List.of("2005-03-04 11:30"),
                "after", List.of("1999-01-01 11:30")
        );
    }

    private Map<String, List<String>> getStringParams() {
        return Map.of(
                "type", List.of("string"),
                "default", List.of("TCP"),
                "list", List.of("TCP", "UDP", "HTTP")
        );
    }

    private Map<String, List<String>> getBooleanParams() {
        return Map.of(
                "type", List.of("boolean"),
                "default", List.of("true")
        );
    }

    private Map<String, List<String>> getMinLessThanMaxParams() {
        return Map.of(
                "type", List.of("number"),
                "min", List.of("10"),
                "max", List.of("1")
        );
    }

    private Map<String, List<String>> getDefaultLessThanMinParams() {
        return Map.of(
                "type", List.of("number"),
                "min", List.of("10"),
                "default", List.of("1")
        );
    }

    private Map<String, List<String>> getDefaultMoreThanMaxParams() {
        return Map.of(
                "type", List.of("number"),
                "default", List.of("10"),
                "max", List.of("1")
        );
    }

    private Map<String, List<String>> getListDoesNotContainDefaultParams() {
        return Map.of(
                "type", List.of("number"),
                "default", List.of("10"),
                "list", List.of("1", "2", "3")
        );
    }

    private Map<String, List<String>> minMaxAndDefaultAreNans() {
        return Map.of(
                "type", List.of("number"),
                "default", List.of("d"),
                "min", List.of("m"),
                "max", List.of("x")
        );
    }

    private Map<String, List<String>> incorrectPatternParams() {
        return Map.of(
                "type", List.of("datetime"),
                "default", List.of("2000-01-01a00:01"),
                "before", List.of("2005-03-04sd 11:30"),
                "after", List.of("1999-01-01 11:30d")
        );
    }

    private Map<String, List<String>> defaultAfterBefore() {
        return Map.of(
                "type", List.of("datetime"),
                "default", List.of("2006-01-01 00:01"),
                "before", List.of("2005-03-04 11:30")
        );
    }

    private Map<String, List<String>> defaultBeforeAfter() {
        return Map.of(
                "type", List.of("datetime"),
                "default", List.of("1998-01-01 00:01"),
                "after", List.of("1999-01-01 11:30")
        );
    }

    private Map<String, List<String>> afterAfterBefore() {
        return Map.of(
                "type", List.of("datetime"),
                "before", List.of("2005-03-04 11:30"),
                "after", List.of("2006-01-01 11:30")
        );
    }

    private Map<String, List<String>> defaultIsNotABoolean() {
        return Map.of(
                "type", List.of("boolean"),
                "default", List.of("truee")
        );
    }

    private Map<String, List<String>> defaultIsNotInList() {
        return Map.of(
                "type", List.of("string"),
                "list", List.of("a", "b", "c"),
                "default", List.of("d")
        );
    }
}
