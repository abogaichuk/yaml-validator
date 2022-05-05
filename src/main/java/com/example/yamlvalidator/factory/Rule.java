package com.example.yamlvalidator.factory;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.ValidationResult;
import com.example.yamlvalidator.validators.ParameterValidator;

public interface Rule {
    ValidationResult validate(ObjectParameter parameter);

    default Rule and(final Rule other) {
        return parameter -> {
            final ValidationResult left = this.validate(parameter);
            final ValidationResult right = other.validate(parameter);

            return left.merge(right);
        };
    }
}
