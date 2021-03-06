package com.example.yamlvalidator.grammar;

import com.example.yamlvalidator.entity.Resource;
import com.example.yamlvalidator.entity.Schema;
import com.example.yamlvalidator.entity.ValidationResult;

@FunctionalInterface
public interface ValidationRule {
    ValidationResult validate(Schema schema, Resource resource);

    static ValidationRule of(final ParameterRule parameterRule) {
        return (schema, resource) -> ValidationRule.of(parameterRule, parameterRule).validate(schema, resource);
    }

    static ValidationRule of(final ParameterRule schemaRule, final ParameterRule resourceRule) {
        return (schema, resource) -> {
            final var schemaResult = schemaRule.validate(schema);

            return resource == null ? schemaResult : schemaResult.merge(resourceRule.validate(resource));
        };
    }

    default ValidationRule and(final ValidationRule other) {
        return (schema, resource) -> {
            final var left = this.validate(schema, resource);
            final var right = other.validate(schema, resource);

            return left.merge(right);
        };
    }

    default ValidationRule or(final ValidationRule other) {
        return (schema, resource) -> {
            final var left = this.validate(schema, resource);

            return left.isValid() ? other.validate(schema, resource) : left;
        };
    }
}
