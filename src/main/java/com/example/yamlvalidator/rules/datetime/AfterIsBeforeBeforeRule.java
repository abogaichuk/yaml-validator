package com.example.yamlvalidator.rules.datetime;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.ValidationResult;
import com.example.yamlvalidator.rules.Rule;
import com.example.yamlvalidator.validators.Conditions;

import static com.example.yamlvalidator.entity.ValidationResult.invalid;
import static com.example.yamlvalidator.utils.ValidatorUtils.toErrorMessage;

public class AfterIsBeforeBeforeRule implements Rule {
    private static final String AFTER = "After";
    private static final String BEFORE = "Before";
    private static final String AFTER_IS_BEFORE = "Validator.After is before Validator.Before";

    @Override
    public ValidationResult validate(ObjectParameter parameter) {
        return parameter.getChildAsString(BEFORE)
                .map(before -> parameter.getChildAsString(AFTER)
                        .filter(after -> Conditions.compareDates.test(before, after))
                        .map(after -> invalid(toErrorMessage(after, AFTER_IS_BEFORE)))
                        .orElseGet(ValidationResult::valid))
                .orElseGet(ValidationResult::valid);
    }
}
