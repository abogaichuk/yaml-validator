package com.example.yamlvalidator.rules.default_param;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.ValidationResult;
import com.example.yamlvalidator.rules.Rule;
import com.example.yamlvalidator.validators.Conditions;

import static com.example.yamlvalidator.entity.ValidationResult.invalid;
import static com.example.yamlvalidator.utils.ValidatorUtils.toErrorMessage;

public class DefaultLessThanMinRule implements Rule {
    private static final String DEFAULT = "Default";
    private static final String VALIDATOR_MIN = "Validators/Min";
    private static final String DEFAULT_LESS_THAN_MIN = "Default < Validators.Min";

    @Override
    public ValidationResult validate(ObjectParameter parameter) {
        return parameter.getChildAsString(VALIDATOR_MIN)
                .map(min -> parameter.getChildAsString(DEFAULT)
                        .filter(max -> Conditions.compareNums.test(min, max))
                        .map(max -> invalid(toErrorMessage(max, DEFAULT_LESS_THAN_MIN)))
                        .orElseGet(ValidationResult::valid))
                .orElseGet(ValidationResult::valid);
    }
}
