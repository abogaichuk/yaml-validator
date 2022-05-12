package com.example.yamlvalidator.rules;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.rules.numbers.MaxValidatorRule;
import com.example.yamlvalidator.rules.numbers.MinValidatorRule;

import java.util.List;

import static com.example.yamlvalidator.entity.ValidationResult.invalid;
import static com.example.yamlvalidator.entity.ValidationResult.valid;
import static com.example.yamlvalidator.utils.ValidatorUtils.*;
import static com.example.yamlvalidator.validators.Conditions.hasDuplicates;
import static com.example.yamlvalidator.validators.Conditions.isBypass;

public class RuleFactory {

    public static Rule getRules(ObjectParameter parameter) {
        return noDuplicates();
    }

//    private Rule of(List<Parameter> parameters) {
//        Rule rule = noDuplicates();
//        if (MIN.equalsIgnoreCase(parameter.getName())) {
//            rule.and(new MinValidatorRule());
//        }
//        if (MAX.equalsIgnoreCase(parameter.getName())) {
//            rule.and(new MaxValidatorRule());
//        }
//        if (M)
//    }

    private static Rule noDuplicates() {
        return parameter -> hasDuplicates.test(parameter) ? invalid(toErrorMessage(parameter, HAS_DUPLICATES)) : valid();
    }

//    private static Rule byPass() {
//        return parameter -> isBypass.test(parameter)
//    }
}
