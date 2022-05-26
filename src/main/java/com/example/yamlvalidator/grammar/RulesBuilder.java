package com.example.yamlvalidator.grammar;

import com.example.yamlvalidator.entity.Param;
import com.example.yamlvalidator.entity.ValidationResult;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static com.example.yamlvalidator.entity.ValidationResult.invalid;
import static com.example.yamlvalidator.utils.ValidatorUtils.toErrorMessage;

public class RulesBuilder {
    private RulesBuilder() {}

    public static SchemaRule singleFieldValidation(String child, String message,
                                                   Predicate<Param> predicate) {
        return param -> param.findChild(child)
                .filter(predicate)
                .map(p -> invalid(toErrorMessage(p, message)))
                .orElseGet(ValidationResult::valid);
    }

    public static SchemaRule doubleFieldsValidation(String child1, String child2, String message,
                                                    BiPredicate<Param, Param> comparator) {
        return param ->  param.findChild(child1)
                .map(p1 -> param.findChild(child2)
                        .filter(p2 -> comparator.test(p1, p2))
                        .map(p2 -> invalid(toErrorMessage(p2, p1, message)))
                        .orElseGet(ValidationResult::valid))
                .orElseGet(ValidationResult::valid);
    }
}
