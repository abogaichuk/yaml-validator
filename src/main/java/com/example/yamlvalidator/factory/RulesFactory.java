package com.example.yamlvalidator.factory;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.StringParameter;
import com.example.yamlvalidator.entity.ValidationResult;
import com.example.yamlvalidator.rules.Rule;
import com.example.yamlvalidator.utils.PadmGrammar;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.example.yamlvalidator.entity.ValidationResult.invalid;
import static com.example.yamlvalidator.entity.ValidationResult.valid;
import static com.example.yamlvalidator.utils.PadmGrammar.*;
import static com.example.yamlvalidator.utils.ValidatorUtils.*;
import static com.example.yamlvalidator.validators.Conditions.*;

public class RulesFactory {
    private final static Map<String, Rule> rules = new HashMap<>();
    static {
        rules.put("number", numbers());
        rules.put("datetime", datetime());
        rules.put("custom", custom());
        rules.put("string", strings());
    }

    public static Rule getRules(ObjectParameter parameter) {
        return rules.getOrDefault(parameter.getTypeChildValue(), custom());
    }

    private static Rule strings() {
        return noDuplicates()
                .and(isFieldInList(LIST_KEY_NAME, DEFAULT_KEY_NAME, DEFAULT_WRONG, contains.negate()));
    }

    private static Rule datetime() {
        return noDuplicates()
                .and(singleFieldValidation(AFTER_KEY_NAME, AFTER_IS_NOT_DATETIME, isDateTime.negate()))
                .and(singleFieldValidation(BEFORE_KEY_NAME, BEFORE_IS_NOT_DATETIME, isDateTime.negate()))
                .and(doubleFieldsValidation(BEFORE_KEY_NAME, AFTER_KEY_NAME, BEFORE_DATE_IS_AFTER, compareDates));
    }

    private static Rule numbers() {
        return noDuplicates()
                .and(singleFieldValidation(MIN_KEY_NAME, MIN_IS_NAN, isNAN))
                .and(singleFieldValidation(MAX_KEY_NAME, MAX_IS_NAN, isNAN))
                .and(doubleFieldsValidation(MIN_KEY_NAME, MAX_KEY_NAME, MAX_LESS_THAN_MIN, compareNums))
                .and(doubleFieldsValidation(MIN_KEY_NAME, DEFAULT_KEY_NAME, DEFAULT_LESS_THAN_MIN, compareNums))
                .and(doubleFieldsValidation(MAX_KEY_NAME, DEFAULT_KEY_NAME, DEFAULT_MORE_THAN_MAX, compareNums.negate()))
                .and(isFieldInList(LIST_KEY_NAME, DEFAULT_KEY_NAME, DEFAULT_WRONG, contains.negate()));
    }

    private static Rule custom() {
        return noDuplicates();
    }

    private static Rule noDuplicates() {
        return parameter -> hasDuplicates.test(parameter) ? invalid(toErrorMessage(parameter, HAS_DUPLICATES)) : valid();
    }

    private static Rule singleFieldValidation(String child, String message, Predicate<StringParameter> predicate) {
        return parameter -> parameter.getChildAsString(child)
                .filter(predicate)
                .map(p -> invalid(toErrorMessage(p, message)))
                .orElseGet(ValidationResult::valid);
    }

    private static Rule doubleFieldsValidation(String child1, String child2, String message,
                                               BiPredicate<StringParameter, StringParameter> comparator) {
        return parameter ->  parameter.getChildAsString(child1)
                .map(p1 -> parameter.getChildAsString(child2)
                        .filter(p2 -> comparator.test(p1, p2))
                        .map(p2 -> invalid(toErrorMessage(p2, message)))
                        .orElseGet(ValidationResult::valid))
                .orElseGet(ValidationResult::valid);
    }

    private static Rule isFieldInList(String child1, String child2, String message,
                                      BiPredicate<List<String>, String> predicate) {
        return parameter -> parameter.findChild(child1)
                .filter(p1 -> p1 instanceof ObjectParameter)
                .map(ObjectParameter.class::cast)
                .map(p1 -> p1.getChildren().stream()
                        .filter(sp -> sp instanceof StringParameter)
                        .map(StringParameter.class::cast)
                        .map(StringParameter::getValue)
                        .collect(Collectors.toList()))
                .map(list -> parameter.getChildAsString(child2)
                        .filter(p2 -> predicate.test(list, p2.getValue()))
                        .map(p2 -> invalid(toErrorMessage(p2, message)))
                        .orElseGet(ValidationResult::valid))
                .orElseGet(ValidationResult::valid);
    }

//    private static Rule oneOf() {
//        return parameter -> parameter.findValidatorParam("OneOf")
//                .map()
//    }
}
