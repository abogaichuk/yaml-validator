package com.example.yamlvalidator.rules.default_param;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.ValidationResult;
import com.example.yamlvalidator.rules.Rule;
import com.example.yamlvalidator.validators.Conditions;

import static com.example.yamlvalidator.entity.ValidationResult.invalid;
import static com.example.yamlvalidator.utils.ValidatorUtils.toErrorMessage;

public class DefaultMoreThanMaxRule implements Rule {
    private static final String DEFAULT = "Default";
    private static final String VALIDATOR_MAX = "Validators/Max";
    private static final String DEFAULT_MORE_THAN_MAX = "Default > Validators.Max";

    @Override
    public ValidationResult validate(ObjectParameter parameter) {
        return parameter.getChildAsString(VALIDATOR_MAX)
                .map(max -> parameter.getChildAsString(DEFAULT)
                        .filter(def -> Conditions.compareNums.negate().test(max, def))
                        .map(def -> invalid(toErrorMessage(def, DEFAULT_MORE_THAN_MAX)))
                        .orElseGet(ValidationResult::valid))
                .orElseGet(ValidationResult::valid);
    }
}
