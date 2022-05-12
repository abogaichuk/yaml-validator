package com.example.yamlvalidator.rules.numbers;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.ValidationResult;
import com.example.yamlvalidator.rules.Rule;
import com.example.yamlvalidator.validators.Conditions;

import static com.example.yamlvalidator.entity.ValidationResult.invalid;
import static com.example.yamlvalidator.utils.ValidatorUtils.toErrorMessage;

public class MinValidatorRule implements Rule {
    private static final String MIN = "Min";
    private static final String MIN_IS_NAN = "Validator.Min is not an number";

    @Override
    public ValidationResult validate(ObjectParameter parameter) {
        return parameter.getChildAsString(MIN)
                .filter(Conditions.isNAN)
                .map(p -> invalid(toErrorMessage(p, MIN_IS_NAN)))
                .orElseGet(ValidationResult::valid);
    }
}
