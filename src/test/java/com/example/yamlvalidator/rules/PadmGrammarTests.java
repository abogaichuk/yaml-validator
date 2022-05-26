//package com.example.yamlvalidator.rules;
//
//import com.example.yamlvalidator.entity.*;
//import org.junit.jupiter.api.Test;
//
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.atomic.AtomicInteger;
//import java.util.stream.Collectors;
//
//import static com.example.yamlvalidator.rules.PadmGrammar.StandardType;
//import static org.junit.jupiter.api.Assertions.*;
//
//public class PadmGrammarTests {
//
//    @Test
//    public void when_correct_returnValid() {
//        assertTrue(StandardType.NUMBER.validate(createParent(getNumberParams())).isValid());
//        assertTrue(StandardType.DATETIME.validate(createParent(getDatetimeParams())).isValid());
//        assertTrue(StandardType.STRING.validate(createParent(getStringParams())).isValid());
//        assertTrue(StandardType.BOOLEAN.validate(createParent(getBooleanParams())).isValid());
//    }
//
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
//
////    @Test
////    public void when_incorrectOneOf_returnInvalid() {
////        assertFalse(StandardType.OBJECT.validate(createParent()).isValid());
////    }
//
//    private ObjectParameter createParent(Map<String, List<String>> params) {
//        var parent = new ObjectParameter("root", Parameter.ParameterType.MAPPING, null, Position.of(0, 1));
//        parent.addChildren(createChildren(parent, params));
//        return parent;
//    }
//
//    private List<Parameter> createChildren(ObjectParameter parent, Map<String, List<String>> params) {
//        var index = new AtomicInteger(1);
//        return params.entrySet().stream()
//                .map(entry -> createChild(parent, entry.getKey(), entry.getValue(), index.getAndIncrement()))
//                .collect(Collectors.toList());
//    }
//
//    private Parameter createChild(ObjectParameter parent, String name, List<String> values, int column) {
//        if (values.size() > 1) {
//            return createObjectParam(parent, name, values, column);
//        } else {
//            return createStringParam(name, values.get(0), column, parent);
//        }
//    }
//
//    private ObjectParameter createObjectParam(ObjectParameter parent, String name, List<String> values, int column) {
//        var parameter = new ObjectParameter(name, Parameter.ParameterType.MAPPING, parent, Position.of(0, column));
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
//
////    private Map<String, Map<String, List<String>>>
//
//    private Map<String, List<String>> getNumberParams() {
//        return Map.of(
//                "type", List.of("number"),
//                "default", List.of("5"),
//                "min", List.of("1"),
//                "max", List.of("10"),
//                "list", List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10")
//                );
//    }
//
//    private Map<String, List<String>> getDatetimeParams() {
//        return Map.of(
//                "type", List.of("datetime"),
//                "default", List.of("2000-01-01 00:01"),
//                "before", List.of("2005-03-04 11:30"),
//                "after", List.of("1999-01-01 11:30")
//        );
//    }
//
//    private Map<String, List<String>> getStringParams() {
//        return Map.of(
//                "type", List.of("string"),
//                "default", List.of("TCP"),
//                "list", List.of("TCP", "UDP", "HTTP")
//        );
//    }
//
//    private Map<String, List<String>> getBooleanParams() {
//        return Map.of(
//                "type", List.of("boolean"),
//                "default", List.of("true")
//        );
//    }
//
//    private Map<String, List<String>> getMinLessThanMaxParams() {
//        return Map.of(
//                "type", List.of("number"),
//                "min", List.of("10"),
//                "max", List.of("1")
//        );
//    }
//
//    private Map<String, List<String>> getDefaultLessThanMinParams() {
//        return Map.of(
//                "type", List.of("number"),
//                "min", List.of("10"),
//                "default", List.of("1")
//        );
//    }
//
//    private Map<String, List<String>> getDefaultMoreThanMaxParams() {
//        return Map.of(
//                "type", List.of("number"),
//                "default", List.of("10"),
//                "max", List.of("1")
//        );
//    }
//
//    private Map<String, List<String>> getListDoesNotContainDefaultParams() {
//        return Map.of(
//                "type", List.of("number"),
//                "default", List.of("10"),
//                "list", List.of("1", "2", "3")
//        );
//    }
//
//    private Map<String, List<String>> minMaxAndDefaultAreNans() {
//        return Map.of(
//                "type", List.of("number"),
//                "default", List.of("d"),
//                "min", List.of("m"),
//                "max", List.of("x")
//        );
//    }
//
//    private Map<String, List<String>> incorrectPatternParams() {
//        return Map.of(
//                "type", List.of("datetime"),
//                "default", List.of("2000-01-01a00:01"),
//                "before", List.of("2005-03-04sd 11:30"),
//                "after", List.of("1999-01-01 11:30d")
//        );
//    }
//
//    private Map<String, List<String>> defaultAfterBefore() {
//        return Map.of(
//                "type", List.of("datetime"),
//                "default", List.of("2006-01-01 00:01"),
//                "before", List.of("2005-03-04 11:30")
//        );
//    }
//
//    private Map<String, List<String>> defaultBeforeAfter() {
//        return Map.of(
//                "type", List.of("datetime"),
//                "default", List.of("1998-01-01 00:01"),
//                "after", List.of("1999-01-01 11:30")
//        );
//    }
//
//    private Map<String, List<String>> afterAfterBefore() {
//        return Map.of(
//                "type", List.of("datetime"),
//                "before", List.of("2005-03-04 11:30"),
//                "after", List.of("2006-01-01 11:30")
//        );
//    }
//
//    private Map<String, List<String>> defaultIsNotABoolean() {
//        return Map.of(
//                "type", List.of("boolean"),
//                "default", List.of("truee")
//        );
//    }
//
//    private Map<String, List<String>> defaultIsNotInList() {
//        return Map.of(
//                "type", List.of("string"),
//                "list", List.of("a", "b", "c"),
//                "default", List.of("d")
//        );
//    }
//}
