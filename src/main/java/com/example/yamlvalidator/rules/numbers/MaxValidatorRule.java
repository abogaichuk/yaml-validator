package com.example.yamlvalidator.rules.numbers;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.ValidationResult;
import com.example.yamlvalidator.rules.Rule;
import com.example.yamlvalidator.validators.Conditions;

import static com.example.yamlvalidator.entity.ValidationResult.invalid;
import static com.example.yamlvalidator.utils.ValidatorUtils.toErrorMessage;

public class MaxValidatorRule implements Rule {
    private static final String MAX = "Max";
    private static final String MAX_IS_NAN = "Validator.Max is not an number";

    @Override
    public ValidationResult validate(ObjectParameter parameter) {
        return parameter.getChildAsString(MAX)
                .filter(Conditions.isNAN)
                .map(p -> invalid(toErrorMessage(p, MAX_IS_NAN)))
                .orElseGet(ValidationResult::valid);
    }
}
