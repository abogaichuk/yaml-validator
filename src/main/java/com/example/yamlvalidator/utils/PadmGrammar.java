package com.example.yamlvalidator.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PadmGrammar {
    public static final String TYPE_KEY_NAME = "type";
    public static final String ITEMS_KEY_NAME = "items";
    public static final String VALIDATORS_KEY_NAME = "validators";
    public static final String ENUM_KEY_NAME = "enum";
    public static final String ONE_OF_KEY_NAME = "oneof";
    public static final String ANY_OF_KEY_NAME = "anyof";
    public static final String PROPERTIES_KEY_NAME = "properties";
    public static final String PATTERN_KEY_NAME = "pattern";
    public static final String DESCRIPTION_KEY_NAME = "description";
    public static final String DEFAULT_KEY_NAME = "default";
    public static final String REQUIRED_KEY_NAME = "required";
    public static final String EXAMPLE_KEY_NAME = "example";
    public static final String MIN_KEY_NAME = "min";
    public static final String MAX_KEY_NAME = "max";
    public static final String LIST_KEY_NAME = "list";
    public static final String AFTER_KEY_NAME = "after";
    public static final String BEFORE_KEY_NAME = "before";

    public static final String OR_KEYWORD = " or ";

    public static final String OBJECT_TYPE = "object";
    public static final String STRING_TYPE = "string";
    public static final String DATETIME_TYPE = "datetime";
    public static final String INTEGER_TYPE = "int";
    public static final String NUMBER_TYPE = "number";
    public static final String BOOLEAN_TYPE = "boolean";
    public static final String SECRET_TYPE= "secret";

    public static List<String> keywords = List.of(TYPE_KEY_NAME, ITEMS_KEY_NAME, VALIDATORS_KEY_NAME, ENUM_KEY_NAME,
            ONE_OF_KEY_NAME, ANY_OF_KEY_NAME, PROPERTIES_KEY_NAME, PATTERN_KEY_NAME, DESCRIPTION_KEY_NAME, DEFAULT_KEY_NAME,
            REQUIRED_KEY_NAME, EXAMPLE_KEY_NAME, MIN_KEY_NAME, MAX_KEY_NAME, LIST_KEY_NAME, AFTER_KEY_NAME, BEFORE_KEY_NAME,
            OR_KEYWORD);

    public static List<String> standardTypes = List.of(OBJECT_TYPE, STRING_TYPE, DATETIME_TYPE,
            INTEGER_TYPE, NUMBER_TYPE, BOOLEAN_TYPE, SECRET_TYPE);


    public static Optional<String> getKeyWord(String word) {
        return find(keywords, word);
    }

    public static Optional<String> getStandardType(String type) {
        return find(standardTypes, type);
    }

    private static Optional<String> find(List<String> list, String value) {
        return list.stream()
                .filter(s -> s.equalsIgnoreCase(value))
                .findAny();
    }

    enum Validators {
        TYPE_KEY_NAME("type"),
        ITEMS_KEY_NAME("items"),
        ENUM_KEY_NAME("enum"),
        ONE_OF_KEY_NAME("oneOf"),
        ANY_OF_KEY_NAME("anyOf"),
        PROPERTIES_KEY_NAME("properties"),
        PATTERN_KEY_NAME("pattern"),
        DESCRIPTION_KEY_NAME("description"),
        DEFAULT_KEY_NAME("default"),
        REQUIRED_KEY_NAME("required"),
        EXAMPLE_KEY_NAME("example"),
        MIN_KEY_NAME("min"),
        MAX_KEY_NAME("max"),
        AFTER_KEY_NAME("after"),
        BEFORE_KEY_NAME("before");

        private final String key;

        public String getKey() {
            return key;
        }

        Validators(String key) {
            this.key = key;
        }
    }
}
