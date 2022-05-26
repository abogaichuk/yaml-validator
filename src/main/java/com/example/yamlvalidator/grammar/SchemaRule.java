package com.example.yamlvalidator.grammar;

import com.example.yamlvalidator.entity.SchemaParam;
import com.example.yamlvalidator.entity.ValidationResult;

@FunctionalInterface
public interface SchemaRule {
    ValidationResult validate(SchemaParam param);

    default SchemaRule and(final SchemaRule other) {
        return parameter -> {
            final ValidationResult left = this.validate(parameter);
            final ValidationResult right = other.validate(parameter);

            return left.merge(right);
        };
    }

    default SchemaRule or(final SchemaRule other) {
        return parameter -> {
            final ValidationResult left = this.validate(parameter);

            return left.isValid() ? other.validate(parameter) : left;
        };
    }
}
