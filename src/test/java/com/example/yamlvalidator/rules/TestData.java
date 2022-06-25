package com.example.yamlvalidator.rules;

import com.example.yamlvalidator.entity.Pair;
import com.example.yamlvalidator.mappers.TestUtils;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.yamlvalidator.mappers.TestUtils.TestType.CUSTOM_STANDARD_TYPE;
import static com.example.yamlvalidator.rules.TestData.ValidationType.*;

public class TestData {
    public static final String WRONG_NUMBER = "test:\n" +
            "  type: number\n" +
            "  default: 1a\n" +
            "  min: 0b\n" +
            "  max: 10c";
    public static final String WRONG_DATETIME = "test:\n" +
            "  type: datetime\n" +
            "  default: 2000-01-01a00:01\n" +
            "  before: dasdas\n" +
            "  after: 123:342";
    public static final String WRONG_BOOLEAN = "test:\n" +
            "  type: boolean\n" +
            "  default: truee";
    public static final String WRONG_STANDARD_TYPE = "test:\n" +
            "  id:\n" +
            "    type: number\n" +
            "    otherUnknownField: number";
    public static final String MAX_LESS_THAN_MIN = "test:\n" +
            "  type: number\n" +
            "  min: 10\n" +
            "  max: 1";
    public static final String DEFAULT_LESS_THAN_MIN = "test:\n" +
            "  type: number\n" +
            "  min: 10\n" +
            "  default: 1";
    public static final String DEFAULT_MORE_THAN_MAX = "test:\n" +
            "  type: number\n" +
            "  max: 1\n" +
            "  default: 2";
    public static final String LIST_DOESNT_CONTAIN_DEFAULT = "test:\n" +
            "  type: number\n" +
            "  list:\n" +
            "    - 0\n" +
            "    - 1\n" +
            "  default: 2";
    public static final String DEFAULT_AFTER_BEFORE = "test:\n" +
            "  type: datetime\n" +
            "  default: 2006-01-01 00:01\n" +
            "  before: 2005-03-04 11:30";
    public static final String DEFAULT_BEFORE_AFTER = "test:\n" +
            "  type: datetime\n" +
            "  default: 1998-01-01 00:01\n" +
            "  after: 1999-01-01 11:30";
    public static final String AFTER_IS_AFTER_BEFORE = "test:\n" +
            "  type: datetime\n" +
            "  before: 2005-03-04 11:30\n" +
            "  after: 2006-01-01 11:30";
    public static final String DUPLICATES = "test:\n" +
            "  default: 1\n" +
            "  default: 2";
    public static final String BYPASS = "test:\n" +
            "  type: number\n" +
            "  default: 1aaaa\n" +
            "  bypass: true";

    //    public static final String  = "";
//    public static final String  = "";

    public enum ValidationType {
        OPTIONAL,
        MANDATORY,
        MANDATORY2,
        ONE_OF,
        ONE_OF2
    }

    public static Map<TestData.ValidationType, Pair<String, String>> validationData = Stream.of(
            new AbstractMap.SimpleImmutableEntry<>(OPTIONAL, new Pair<>(
                    "test:\n" +
                    "  id:\n" +
                    "    type: number\n" +
                    "    optional: true\n" +
                    "  name:\n" +
                    "    type: string\n" +
                    "    optional: true",
                    "")),
            new AbstractMap.SimpleImmutableEntry<>(MANDATORY, new Pair<>(
                    "test:\n" +
                    "  id:\n" +
                    "    type: number\n",
                    "")),
            new AbstractMap.SimpleImmutableEntry<>(MANDATORY2, new Pair<>(
                    "test:\n" +
                            "  id:\n" +
                            "    type: number\n" +
                            "  executeddate:\n" +
                            "    type: datetime\n" +
                            "  user:\n" +
                            "    type: string\n" +
                            "    list:\n" +
                            "      - system\n" +
                            "      - devops",
                    "test:\n" +
                            "  id: 1\n" +
                            "  executedDate: 2005-03-04 11:30")),
            new AbstractMap.SimpleImmutableEntry<>(ONE_OF, new Pair<>(
                    "test:\n" +
                    "  oneOf:\n" +
                    "    - type: number\n" +
                    "    - type: boolean",
                    "test: aaa")),
            new AbstractMap.SimpleImmutableEntry<>(ONE_OF2, new Pair<>(
                    "test:\n" +
                    "  oneOf:\n" +
                    "    - type: number\n" +
                    "    - type: object\n" +
                    "      id:\n" +
                    "        type: number\n" +
                    "      executedDate:\n" +
                    "        type: datetime\n" +
                    "      user:\n" +
                    "        type: string\n" +
                    "        list:\n" +
                    "          - system\n" +
                    "          - devops",
                    "test:\n" +
                    "  id: 1\n" +
                    "  executedDate: 2005-03-04 11:30"))
            ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
}
