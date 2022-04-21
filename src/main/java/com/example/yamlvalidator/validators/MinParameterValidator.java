package com.example.yamlvalidator.validators;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.StringParameter;
import com.example.yamlvalidator.entity.ValidationError;

import java.util.Optional;

public class MinParameterValidator extends AbstractParameterValidator implements ParameterValidator {
    private final String message = "Validator.Min is not an integer";

    @Override
    public Optional<ValidationError> validate(ObjectParameter parameter) {
        //todo default compare with validators
        //todo default compare with type
        return findValidatorByName(MIN, parameter)
                .flatMap(intParam -> canBeParsedToInt((StringParameter) intParam, message));
    }
}
