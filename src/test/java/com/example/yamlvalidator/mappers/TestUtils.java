package com.example.yamlvalidator.mappers;

import com.example.yamlvalidator.entity.Pair;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.yamlvalidator.mappers.TestUtils.TestType.*;

public class TestUtils {
    public enum TestType {
        CUSTOM_STANDARD_TYPE,
        CUSTOM_STANDARD_TYPE2,
        CUSTOM_TYPE,
        CUSTOM_TYPE3_MERGING,
        CUSTOM_TYPE4_RECURSIVE,
        CUSTOM_TYPE_LIST,
        CUSTOM_PRIMITIVES,
        VARIANT_TYPE,
        VARIANT_TYPE2,
        SEQUENCE_TYPE,
        SEQUENCE_TYPE2;
    }

    public static Map<TestType, Pair<String, String>> data = Stream.of(
                new AbstractMap.SimpleImmutableEntry<>(CUSTOM_STANDARD_TYPE, new Pair<>(
                        "types:\n" +
                        "  Admin: number\n" +
                        "user:\n" +
                        "  type: Admin",
                        "user:\n" +
                        "  type: number")),
                new AbstractMap.SimpleImmutableEntry<>(CUSTOM_STANDARD_TYPE2, new Pair<>(
                        "types:\n" +
                        "  Admin: number\n" +
                        "user: Admin",
                        "user:\n" +
                        "  type: number")),
            new AbstractMap.SimpleImmutableEntry<>(CUSTOM_TYPE, new Pair<>(
                    "types:\n" +
                    "  Admin:\n" +
                    "    type: string\n" +
                    "    list:\n" +
                    "      - system\n" +
                    "      - devops\n" +
                    "user: admin",
                    "user:\n" +
                    "  type: string\n" +
                    "  list:\n" +
                    "  - system\n" +
                    "  - devops"
            )),
            new AbstractMap.SimpleImmutableEntry<>(CUSTOM_TYPE3_MERGING, new Pair<>(
                    "types:\n" +
                    "  Admin:\n" +
                    "    type: string\n" +
                    "    list:\n" +
                    "    - system\n" +
                    "    - devops\n" +
                    "user:\n" +
                    "  type: admin\n" +
                    "  list:\n" +
                    "  - qa\n" +
                    "  - hr",
                    "user:\n" +
                    "  type: string\n" +
                    "  list:\n" +
                    "  - qa\n" +
                    "  - hr"
            )),
            new AbstractMap.SimpleImmutableEntry<>(CUSTOM_TYPE4_RECURSIVE, new Pair<>(
                    "types:\n" +
                    "  Genesys:\n" +
                    "    SwitchName:\n" +
                    "      SwitchLastName: string\n" +
                    "      SwitchFirstName:\n" +
                    "        SwitchFirstFirstName: string\n" +
                    "        SwitchSecondSecond:\n" +
                    "          type: boolean or number\n" +
                    "device: Genesys",
                    "device:\n" +
                    "  switchname:\n" +
                    "    switchlastname:\n" +
                    "      type: string\n" +
                    "    switchfirstname:\n" +
                    "      switchfirstfirstname:\n" +
                    "        type: string\n" +
                    "      switchsecondsecond:\n" +
                    "        oneof:\n" +
                    "        - type: boolean\n" +
                    "        - type: number"
            )),
            new AbstractMap.SimpleImmutableEntry<>(CUSTOM_TYPE_LIST, new Pair<>(
                    "", ""
            )),
            new AbstractMap.SimpleImmutableEntry<>(CUSTOM_PRIMITIVES, new Pair<>(
                    "user1: number\n" +
                    "user2: string\n" +
                    "user3: datetime\n" +
                    "user4: boolean",
                    "user1:\n" +
                    "  type: number\n" +
                    "user2:\n" +
                    "  type: string\n" +
                    "user3:\n" +
                    "  type: datetime\n" +
                    "user4:\n" +
                    "  type: boolean")),
            new AbstractMap.SimpleImmutableEntry<>(VARIANT_TYPE, new Pair<>(
                    "test: string or number",
                    "test:\n" +
                    "  oneof:\n" +
                    "  - type: string\n" +
                    "  - type: number"
            )),
            new AbstractMap.SimpleImmutableEntry<>(VARIANT_TYPE2, new Pair<>(
                    "types:\n" +
                    "  admin:\n" +
                    "    type: string\n" +
                    "    list:\n" +
                    "      - system\n" +
                    "      - devops\n" +
                    "  autoTest: number\n" +
                    "  manualTest:\n" +
                    "    id: number\n" +
                    "    executedDate: datetime\n" +
                    "    user: Admin\n" +
                    "test: autoTest or manualTest",
                    "test:\n" +
                    "  oneof:\n" +
                    "  - type: number\n" +
                    "  - id:\n" +
                    "      type: number\n" +
                    "    executeddate:\n" +
                    "      type: datetime\n" +
                    "    user:\n" +
                    "      type: string\n" +
                    "      list:\n" +
                    "      - system\n" +
                    "      - devops"
            )),
            new AbstractMap.SimpleImmutableEntry<>(SEQUENCE_TYPE, new Pair<>(
                    "types:\n" +
                    "  Environment:\n" +
                    "    local: string\n" +
                    "    remote: string\n" +
                    "  Argument:\n" +
                    "    short: string\n" +
                    "    long: string\n" +
                    "Array:\n" +
                    "  - Type: string\n" +
                    "    Description: SequenceGroup with custom objects Test.\n" +
                    "  - Type: Environment\n" +
                    "  - Type: Argument",
                    "array:\n" +
                    "- type: string\n" +
                    "  description: SequenceGroup with custom objects Test.\n" +
                    "- local:\n" +
                            "    type: string\n" +
                    "  remote:\n" +
                            "    type: string\n" +
                    "- short:\n" +
                            "    type: string\n" +
                    "  long:\n" +
                            "    type: string"
            )),
            new AbstractMap.SimpleImmutableEntry<>(SEQUENCE_TYPE2, new Pair<>(
                    "types:\n" +
                    "  User:\n" +
                    "    id: number\n" +
                    "    fullname: string\n" +
                    "Moderators:\n" +
                    "  - type: User\n" +
                    "  - type: object\n" +
                    "    id: number\n" +
                    "    login: string",
                    "moderators:\n" +
                    "- id:\n" +
                    "    type: number\n" +
                    "  fullname:\n" +
                    "    type: string\n" +
                    "- type: object\n" +
                    "  id:\n" +
                    "    type: number\n" +
                    "  login:\n" +
                    "    type: string"
            ))
            ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
}
