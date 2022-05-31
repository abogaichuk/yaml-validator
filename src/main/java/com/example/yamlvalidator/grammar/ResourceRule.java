package com.example.yamlvalidator.grammar;

import com.example.yamlvalidator.entity.Resource;
import com.example.yamlvalidator.entity.SchemaParam;
import com.example.yamlvalidator.entity.ValidationResult;

@FunctionalInterface
public interface ResourceRule {
    ValidationResult validate(Resource param);

    default ResourceRule and(final ResourceRule other) {
        return parameter -> {
            final ValidationResult left = this.validate(parameter);
            final ValidationResult right = other.validate(parameter);

            return left.merge(right);
        };
    }

    default ResourceRule or(final ResourceRule other) {
        return parameter -> {
            final ValidationResult left = this.validate(parameter);

            return left.isValid() ? other.validate(parameter) : left;
        };
    }
}
