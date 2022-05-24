package com.example.yamlvalidator.rules;

import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.StringParameter;
import com.example.yamlvalidator.entity.ValidationResult;
import org.springframework.lang.Nullable;

@FunctionalInterface
public interface ValidationRule<D extends Parameter, R extends Parameter> {
    ValidationResult validate(D definition, @Nullable R resource);

    default ValidationRule<D, R> and(final ValidationRule<D, R> other) {
        return (schema, resource) -> {
            final ValidationResult left = this.validate(schema, resource);
            final ValidationResult right = other.validate(schema, resource);

            return left.merge(right);
        };
    }

    default ValidationRule<D, R> or(final ValidationRule<D, R> other) {
        return (schema, resource) -> {
            final ValidationResult left = this.validate(schema, resource);

            return left.isValid() ? other.validate(schema, resource) : left;
        };
    }

//    default StringParameter castToScalar(Parameter p) {
//        return p instanceof StringParameter ? (StringParameter) p : null;
//    }
}
