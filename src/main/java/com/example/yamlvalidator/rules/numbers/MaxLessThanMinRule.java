package com.example.yamlvalidator.rules.numbers;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.ValidationResult;
import com.example.yamlvalidator.rules.Rule;
import com.example.yamlvalidator.validators.Conditions;

import static com.example.yamlvalidator.entity.ValidationResult.invalid;
import static com.example.yamlvalidator.utils.ValidatorUtils.toErrorMessage;

public class MaxLessThanMinRule implements Rule {
    private static final String MIN = "Min";
    private static final String MAX = "Max";
    private static final String MAX_LESS_THAN_MIN = "Validator.Max < Validators.Min";

    @Override
    public ValidationResult validate(ObjectParameter parameter) {
        return parameter.getChildAsString(MIN)
                .map(min -> parameter.getChildAsString(MAX)
                        .filter(max -> Conditions.compareNums.test(min, max))
                        .map(max -> invalid(toErrorMessage(max, MAX_LESS_THAN_MIN)))
                        .orElseGet(ValidationResult::valid))
                .orElseGet(ValidationResult::valid);
    }
}
