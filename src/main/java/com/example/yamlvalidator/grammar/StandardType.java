package com.example.yamlvalidator.grammar;

import java.util.Arrays;
import java.util.function.Function;

public enum StandardType {
    OBJECT(RuleService::objects),
    STRING(RuleService::strings),
    DATETIME(RuleService::datetime),
    NUMBER(RuleService::numbers),
    BOOLEAN(RuleService::booleans),
    SECRET(RuleService::objects);
//    CUSTOM(RuleService::customs);

    public final Function<RuleService, ValidationRule> ruleFunction;
    StandardType(Function<RuleService, ValidationRule> ruleFunction) {
        this.ruleFunction = ruleFunction;
    }

    public static StandardType getOrDefault(String name) {
        return Arrays.stream(StandardType.values())
                .filter(value -> value.name().equalsIgnoreCase(name))
                .findAny().orElse(OBJECT);
    }
}
