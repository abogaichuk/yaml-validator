package com.example.yamlvalidator.rules.default_param;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.StringParameter;
import com.example.yamlvalidator.entity.ValidationResult;
import com.example.yamlvalidator.rules.Rule;
import com.example.yamlvalidator.validators.Conditions;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.yamlvalidator.entity.ValidationResult.invalid;
import static com.example.yamlvalidator.utils.ValidatorUtils.toErrorMessage;

public class DefaultInListRule implements Rule {
    private static final String VALIDATOR_LIST = "Validators/List";
    private static final String DEFAULT = "Default";
    private static final String DEFAULT_WRONG = "Validators.List doesn't contain Default value";

    @Override
    public ValidationResult validate(ObjectParameter parameter) {
        return extractList(parameter)
                .map(list -> parameter.getChildAsString(DEFAULT)
                        .filter(def -> Conditions.contains.test(list, def.getValue()))
                        .map(def -> invalid(toErrorMessage(def, DEFAULT_WRONG)))
                        .orElseGet(ValidationResult::valid))
                .orElseGet(ValidationResult::valid);
    }

    private Optional<List<String>> extractList(ObjectParameter parameter) {
        return parameter.findValidatorParam(VALIDATOR_LIST)
                .filter(p1 -> p1 instanceof ObjectParameter)
                .map(ObjectParameter.class::cast)
                .map(p1 -> p1.getChildren().stream()
                        .filter(sp -> sp instanceof StringParameter)
                        .map(StringParameter.class::cast)
                        .map(StringParameter::getValue)
                        .collect(Collectors.toList()));
    }
}
