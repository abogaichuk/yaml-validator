package com.example.yamlvalidator.rules;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.StringParameter;
import com.example.yamlvalidator.entity.ValidationResult;
import com.example.yamlvalidator.utils.PadmGrammar;

import java.util.List;
import java.util.Optional;


public class ValidatorsParameterRule implements Rule{
    private StringParameter defaultParameter;
    private StringParameter typeParameter;

    @Override
    public ValidationResult validate(ObjectParameter parameter) {
        Optional<ValidationResult> result = parameter.findChild(PadmGrammar.VALIDATORS_KEY_NAME)
                .map(ObjectParameter.class::cast)
                .map(ObjectParameter::getChildren)
                .map(this::validateFunction);
        return result.orElseGet(ValidationResult::valid);
    }

    private ValidationResult validateFunction(final List<Parameter> validators) {
        return ValidationResult.valid();
    }
}
