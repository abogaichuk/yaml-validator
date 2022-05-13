package com.example.yamlvalidator.rules;

import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.ValidationResult;

@FunctionalInterface
public interface ParameterRule<T extends Parameter> {
    ValidationResult validate(T parameter);

    default ParameterRule<T> and(final ParameterRule<T> other) {
        return parameter -> {
            final ValidationResult left = this.validate(parameter);
            final ValidationResult right = other.validate(parameter);

            return left.merge(right);
        };
    }

    default ParameterRule<T> or(final ParameterRule<T> other) {
        return parameter -> {
            final ValidationResult left = this.validate(parameter);

            return left.isValid() ? other.validate(parameter) : left;
        };
    }
}
