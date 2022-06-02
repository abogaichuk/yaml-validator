package com.example.yamlvalidator.grammar;

import com.example.yamlvalidator.entity.Param;
import com.example.yamlvalidator.entity.Resource;
import com.example.yamlvalidator.entity.ValidationResult;

@FunctionalInterface
public interface ValidationRule {
    ValidationResult validate(Param schema, Resource resource);

    static ValidationRule of(final ParameterRule parameterRule) {
        return (schema, resource) -> {
          final ValidationResult schemaResult = parameterRule.validate(schema);

          return resource == null ? schemaResult : schemaResult.merge(parameterRule.validate(resource));
        };
    }

    static ValidationRule of(final ParameterRule schemaRule, final ParameterRule resourceRule) {
        return (schema, resource) -> {
            final ValidationResult schemaResult = schemaRule.validate(schema);
            final ValidationResult resourceResult = resourceRule.validate(resource);

            return schemaResult.merge(resourceResult);
        };
    }

    default ValidationRule and(final ValidationRule other) {
        return (schema, resource) -> {
            final ValidationResult left = this.validate(schema, resource);
            final ValidationResult right = other.validate(schema, resource);

            return left.merge(right);
        };
    }

    default ValidationRule or(final ValidationRule other) {
        return (schema, resource) -> {
            final ValidationResult left = this.validate(schema, resource);

            return left.isValid() ? other.validate(schema, resource) : left;
        };
    }
}
