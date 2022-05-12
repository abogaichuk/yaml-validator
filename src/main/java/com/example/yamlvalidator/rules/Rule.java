package com.example.yamlvalidator.rules;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.StringParameter;
import com.example.yamlvalidator.entity.ValidationResult;

public interface Rule {
    ValidationResult validate(ObjectParameter parameter);

    default Rule and(final Rule other) {
        return parameter -> {
            final ValidationResult left = this.validate(parameter);
            final ValidationResult right = other.validate(parameter);

            return left.merge(right);
        };
    }

    default boolean shouldSkip(ObjectParameter parameter) {
        return parameter.findChild("bypass")
                .map(StringParameter.class::cast)
                .map(StringParameter::getValue)
                .map(Boolean::parseBoolean)
                .orElse(Boolean.FALSE);
    }
}
