package com.example.yamlvalidator.rules.datetime;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.ValidationResult;
import com.example.yamlvalidator.rules.Rule;
import com.example.yamlvalidator.validators.Conditions;

import static com.example.yamlvalidator.entity.ValidationResult.invalid;
import static com.example.yamlvalidator.utils.ValidatorUtils.toErrorMessage;

public class BeforeValidatorRule implements Rule {
    private static final String BEFORE = "Before";
    private static final String BEFORE_IS_NOT_DATETIME = "Validator.Before is not a datetime";

    @Override
    public ValidationResult validate(ObjectParameter parameter) {
        return parameter.getChildAsString(BEFORE)
                .filter(Conditions.isDateTime.negate())
                .map(p -> invalid(toErrorMessage(p, BEFORE_IS_NOT_DATETIME)))
                .orElseGet(ValidationResult::valid);
    }
}
