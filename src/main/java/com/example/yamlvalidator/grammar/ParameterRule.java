package com.example.yamlvalidator.grammar;

import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.ValidationResult;

@FunctionalInterface
public interface ParameterRule {
    ValidationResult validate(Parameter param);

    default ParameterRule and(final ParameterRule other) {
        return parameter -> {
            final var left = this.validate(parameter);
            final var right = other.validate(parameter);

            return left.merge(right);
        };
    }

    default ParameterRule or(final ParameterRule other) {
        return parameter -> {
            final var left = this.validate(parameter);

            return left.isValid() ? other.validate(parameter) : left;
        };
    }
}
