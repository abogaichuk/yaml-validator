package com.example.yamlvalidator.validators;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.StringParameter;
import com.example.yamlvalidator.entity.ValidationError;

import java.util.Optional;

public class MaxParameterValidator extends AbstractParameterValidator implements ParameterValidator {
    private final String message = "Validator.Max is not a number";

    @Override
    public Optional<ValidationError> validate(ObjectParameter parameter) {
        return findValidatorByName(MAX, parameter)
                .flatMap(intParam -> canBeParsedToInt((StringParameter) intParam, message));
    }
}
