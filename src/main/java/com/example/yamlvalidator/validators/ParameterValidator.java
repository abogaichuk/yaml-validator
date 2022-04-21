package com.example.yamlvalidator.validators;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.ValidationError;

import java.util.List;
import java.util.Optional;

public interface ParameterValidator {
    Optional<ValidationError> validate(ObjectParameter parameter);
}
