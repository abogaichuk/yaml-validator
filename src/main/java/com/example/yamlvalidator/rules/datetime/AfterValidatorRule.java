package com.example.yamlvalidator.rules.datetime;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.ValidationResult;
import com.example.yamlvalidator.rules.Rule;
import com.example.yamlvalidator.validators.Conditions;

import static com.example.yamlvalidator.entity.ValidationResult.invalid;
import static com.example.yamlvalidator.utils.ValidatorUtils.toErrorMessage;

public class AfterValidatorRule implements Rule {
    private static final String AFTER = "After";
    private static final String AFTER_IS_NOT_DATETIME = "Validator.After is not a datetime";

    @Override
    public ValidationResult validate(ObjectParameter parameter) {
        return parameter.getChildAsString(AFTER)
                .filter(Conditions.isDateTime.negate())
                .map(p -> invalid(toErrorMessage(p, AFTER_IS_NOT_DATETIME)))
                .orElseGet(ValidationResult::valid);
    }
}
