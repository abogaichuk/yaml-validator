package com.example.yamlvalidator.rules;

import com.example.yamlvalidator.entity.*;
import com.example.yamlvalidator.grammar.KeyWord;
import com.example.yamlvalidator.grammar.RuleService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;


public class RuleServiceTests {

    RuleService rules = new RuleService();

    @Test
    public void when_correct_returnValid() {
        assertTrue(createSchema(getNumberParams()).validate(rules, null).isValid());
        assertTrue(createSchema(getDatetimeParams()).validate(rules, null).isValid());
        assertTrue(createSchema(getStringParams()).validate(rules, null).isValid());
        assertTrue(createSchema(getBooleanParams()).validate(rules, null).isValid());
    }

    @Test
    public void when_incorrectNumber_returnInvalid() {
        assertFalse(createSchema(getMinLessThanMaxParams()).validate(rules, null).isValid());
        assertFalse(createSchema(getDefaultLessThanMinParams()).validate(rules, null).isValid());
        assertFalse(createSchema(getDefaultMoreThanMaxParams()).validate(rules, null).isValid());
        assertFalse(createSchema(getListDoesNotContainDefaultParams()).validate(rules, null).isValid());

        var result = createSchema(minMaxAndDefaultAreNans()).validate(rules, null);
        assertEquals(3, result.getReasons().size());
    }

    @Test
    public void when_incorrectDatetime_returnInvalid() {
        assertEquals(3, createSchema(incorrectPatternParams()).validate(rules, null).getReasons().size());

        assertFalse(createSchema(defaultAfterBefore()).validate(rules, null).isValid());
        assertFalse(createSchema(defaultBeforeAfter()).validate(rules, null).isValid());
        assertFalse(createSchema(afterAfterBefore()).validate(rules, null).isValid());
    }

    @Test
    public void when_incorrectBoolean_returnInvalid() {
        assertFalse(createSchema(defaultIsNotABoolean()).validate(rules, null).isValid());
    }

    @Test
    public void when_incorrectString_returnInvalid() {
        assertFalse(createSchema(defaultIsNotInList()).validate(rules, null).isValid());
    }

    @Test
    public void when_incorrectOneOf_returnInvalid() {
        var oneOf = createOneOfSchema();
        var resource = createResource(stringValue());
        assertFalse(oneOf.validate(rules, resource).isValid());
    }

    @Test
    public void when_correctOneOf_returnValid() {
        //todo oneOf custom objects
        var oneOf = createOneOfSchema();
        var resource = createResource(booleanValue());
        assertTrue(oneOf.validate(rules, resource).isValid());
    }

    @Test
    public void when_paramIsOptional_returnValid() {
//        assertFalse(StandardType.OBJECT.validate(createParent()).isValid());
    }

    @Test
    public void when_hasDuplicates_returnInvalid() {
        var schema = createSchema(getCustomParams());
        schema.addChild(createChild(schema, "aaa", List.of("dasd")));
        assertFalse(schema.validate(rules, null).isValid());
    }

    @Test
    public void when_isBypass_returnValid() {
        var schema = createSchema(getByPass());
        var resource = createResource(stringValue());

        assertTrue(schema.validate(rules, resource)
                .getReasons().stream()
                .anyMatch(message -> message.contains("validation is skipped")));
    }

    private Schema createOneOfSchema() {
        var root = new Schema("", "", null, Position.of(0, 1), Param.YamlType.MAPPING);
        var param = new SchemaParam("test", "", root, null, Param.YamlType.SEQUENCE);
        var oneOf = new SchemaParam(KeyWord.ONEOF.name(), "", param, null, Param.YamlType.SEQUENCE);
        param.addChild(oneOf);

        oneOf.addChild(new SchemaParam("", "number", oneOf, null, Param.YamlType.SCALAR));
        oneOf.addChild(new SchemaParam("", "boolean", oneOf, null, Param.YamlType.SCALAR));

        root.addChild(param);
        return root;
    }

    private Resource createResource(Map<String, List<String>> params) {
        var root = new Resource("", "", null, Position.of(0, 1), Param.YamlType.MAPPING);
        root.addChildren(createChildren(root, params));
        return root;
    }

    private Schema createSchema(Map<String, List<String>> params) {
        var parent = new Schema("", "", null, Position.of(0, 1), Param.YamlType.MAPPING);
        parent.addChildren(createChildren(parent, params));
        return parent;
    }

    private List<Param> createChildren(Param parent, Map<String, List<String>> params) {
        return params.entrySet().stream()
                .map(entry -> createChild(parent, entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private Param createChild(Param parent, String name, List<String> values) {
        if (values.size() > 1) {
            var param = createParam(name, "", parent, Param.YamlType.MAPPING);
            for (var v : values) {
                param.addChild(createChild(param, "", List.of(v)));
            }
            return param;
        } else {
            return createParam(name, values.get(0), parent, Param.YamlType.SCALAR);
        }
    }

    private Param createParam(String name, String value, Param parent, Param.YamlType type) {
        if (parent instanceof SchemaParam) {
            return new SchemaParam(name, value, parent, null, type);
        } else {
            return new Resource(name, value, parent, null, type);
        }
    }

    private Map<String, List<String>> getCustomParams() {
        return Map.of(
                "aaa", List.of("asd"),
                "bbb", List.of("asd")
        );
    }

    private Map<String, List<String>> getByPass() {
        return Map.of(
                "bypass", List.of("true"),
                "invalidField", List.of("invalid")
        );
    }

    private Map<String, List<String>> getNumberParams() {
        return Map.of(
                "type", List.of("number"),
                "default", List.of("5"),
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

    private Map<String, List<String>> stringValue() {
        return Map.of(
                "test", List.of("some string")
        );
    }

    private Map<String, List<String>> booleanValue() {
        return Map.of(
                "test", List.of("true")
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
