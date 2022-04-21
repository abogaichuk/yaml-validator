package com.example.yamlvalidator.validators;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.ValidationError;

import java.time.LocalDateTime;
import java.util.*;

public class DefaultHasCorrectTypeValidator extends AbstractParameterValidator implements ParameterValidator {
    private final String message = "Default Parameter has incorrect type";

    //todo datetime format?, secret, object, array, different type names like bool or int?
    @Override
    public Optional<ValidationError> validate(ObjectParameter parameter) {
        Optional<? extends Parameter> defaultP = findChild(DEFAULT, parameter);
        Optional<? extends Parameter> type = findChild(TYPE, parameter);
        Optional<? extends Parameter> format = findChild(FORMAT, parameter);
        if (type.isPresent() && defaultP.isPresent()) {
            try {
                if ("boolean".equalsIgnoreCase(getValue(type.get()))) {
                    Boolean.parseBoolean(getValue(defaultP.get()));
                } else if ("number".equalsIgnoreCase(getValue(type.get()))) {
                    Integer.parseInt(getValue(defaultP.get()));
                } else if ("datetime".equalsIgnoreCase(getValue(defaultP.get())) && format.isPresent()) {
                    LocalDateTime.parse(getValue(defaultP.get()));
                }
            } catch (RuntimeException e) {
                return Optional.of(ValidationError.of(message, defaultP.get().getName(), defaultP.get().getColumn()));
            }

        }
        return Optional.empty();
    }
}
