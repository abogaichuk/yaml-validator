package com.example.yamlvalidator.factory;

import com.example.yamlvalidator.entity.ObjectParameter;

import java.util.HashMap;
import java.util.Map;

import static com.example.yamlvalidator.entity.ValidationResult.invalid;
import static com.example.yamlvalidator.entity.ValidationResult.valid;
import static com.example.yamlvalidator.utils.ValidatorUtils.*;
import static com.example.yamlvalidator.validators.Conditions.compareDates;
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
                .and(new ListContainsRule(VALIDATOR_LIST, DEFAULT, DEFAULT_WRONG, contains.negate()));
    }

    private static Rule datetime() {
        return noDuplicates()
                .and(new ChildRule(VALIDATOR_AFTER, AFTER_IS_NOT_DATETIME, isDateTime.negate()))
                .and(new ChildRule(VALIDATOR_BEFORE, BEFORE_IS_NOT_DATETIME, isDateTime.negate()))
                .and(new ComparingChildRule(VALIDATOR_BEFORE, VALIDATOR_AFTER, BEFORE_DATE_IS_AFTER, compareDates));
    }

    private static Rule numbers() {
        return noDuplicates()
                .and(new ChildRule(VALIDATOR_MIN, MIN_IS_NAN, isNAN))
                .and(new ChildRule(VALIDATOR_MAX, MAX_IS_NAN, isNAN))
                .and(new ComparingChildRule(VALIDATOR_MIN, VALIDATOR_MAX, MAX_LESS_THAN_MIN, compareNums))
                .and(new ComparingChildRule(VALIDATOR_MIN, DEFAULT, DEFAULT_LESS_THAN_MIN, compareNums))
                .and(new ComparingChildRule(VALIDATOR_MAX, DEFAULT, DEFAULT_MORE_THAN_MAX, compareNums.negate()))
                .and(new ListContainsRule(VALIDATOR_LIST, DEFAULT, DEFAULT_WRONG, contains.negate()));
    }

    private static Rule custom() {
        return noDuplicates();
    }

    private static Rule noDuplicates() {
        return parameter -> hasDuplicates.test((ObjectParameter) parameter) ? invalid(toErrorMessage(parameter, HAS_DUPLICATES)) : valid();
    }

//    private static Rule oneOf() {
//        return parameter -> parameter.findValidatorParam("OneOf")
//                .map()
//    }
}
