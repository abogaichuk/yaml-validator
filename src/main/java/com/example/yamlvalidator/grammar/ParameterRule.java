package com.example.yamlvalidator.grammar;

import com.example.yamlvalidator.entity.Param;
import com.example.yamlvalidator.entity.SchemaParam;
import com.example.yamlvalidator.entity.ValidationResult;

@FunctionalInterface
public interface ParameterRule {
    ValidationResult validate(Param param);

    default ParameterRule and(final ParameterRule other) {
        return parameter -> {
            final ValidationResult left = this.validate(parameter);
            final ValidationResult right = other.validate(parameter);

            return left.merge(right);
        };
    }

    default ParameterRule or(final ParameterRule other) {
        return parameter -> {
            final ValidationResult left = this.validate(parameter);

            return left.isValid() ? other.validate(parameter) : left;
        };
    }
}
