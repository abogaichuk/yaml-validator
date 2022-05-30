package com.example.yamlvalidator.grammar;

import com.example.yamlvalidator.entity.Param;
import com.example.yamlvalidator.entity.Resource;
import com.example.yamlvalidator.entity.ValidationResult;

@FunctionalInterface
public interface ValidationRule {
    ValidationResult validate(Param param, Resource resource);

    default ValidationRule and(final ValidationRule other) {
        return (schemaParam, resource) -> {
            final ValidationResult left = this.validate(schemaParam, resource);
            final ValidationResult right = other.validate(schemaParam, resource);

            return left.merge(right);
        };
    }

    default ValidationRule or(final ValidationRule other) {
        return (schemaParam, resource) -> {
            final ValidationResult left = this.validate(schemaParam, resource);

            return left.isValid() ? other.validate(schemaParam, resource) : left;
        };
    }
}
