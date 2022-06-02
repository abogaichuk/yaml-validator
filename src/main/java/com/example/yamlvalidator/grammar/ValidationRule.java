package com.example.yamlvalidator.grammar;

import com.example.yamlvalidator.entity.Param;
import com.example.yamlvalidator.entity.Resource;
import com.example.yamlvalidator.entity.SchemaParam;
import com.example.yamlvalidator.entity.ValidationResult;
import org.springframework.lang.Nullable;

import java.util.Objects;
import java.util.function.Function;

@FunctionalInterface
public interface ValidationRule {
    ValidationResult validate(Param param, Resource resource);

    static ValidationRule of(final ParameterRule parameterRule) {
        return (schemaParam, resource) -> {
          final ValidationResult schemaResult = parameterRule.validate(schemaParam);

          return resource == null ? schemaResult : schemaResult.merge(parameterRule.validate(resource));
        };
    }

    static ValidationRule of(final ParameterRule schemaRule, final ParameterRule resourceRule) {
        return (schemaParam, resource) -> {
            final ValidationResult schemaResult = schemaRule.validate(schemaParam);
            final ValidationResult resourceResult = resourceRule.validate(resource);

            return schemaResult.merge(resourceResult);
        };
    }

//    static ValidationRule of(
//            final Function<Param, ValidationResult> schemaRule,
//            @Nullable final Function<Param, ValidationResult> resourceRule) {
//        Objects.requireNonNull(schemaRule);
//        return (schemaParam, resource) -> {
//            final ValidationResult schemaResult = schemaRule.apply(schemaParam);
//
//            return resourceRule == null ? schemaResult : schemaResult.merge(resourceRule.apply(resource));
//        };
//    }

//    static ValidationRule of(
//            final SchemaRule schemaRule,
//            @Nullable final Function<Param, ValidationResult> resourceRule) {
//        Objects.requireNonNull(schemaRule);
//        return (schemaParam, resource) -> {
//            final ValidationResult schemaResult = schemaRule.validate((SchemaParam) schemaParam);
//
//            return resourceRule == null ? schemaResult : schemaResult.merge(resourceRule.apply(resource));
//        };
//    }

//    static ValidationRule of(final SchemaRule schemaRule, @Nullable final ParameterRule resourceRule) {
//        Objects.requireNonNull(schemaRule);
//        return (schemaParam, resource) -> {
//            final ValidationResult schemaResult = schemaRule.validate((SchemaParam) schemaParam);
//
//            return resourceRule == null ? schemaResult : schemaResult.merge(resourceRule.validate(resource));
//        };
//    }

//    static ValidationRule of(final SchemaRule schemaRule, @Nullable final ResourceRule resourceRule) {
//        Objects.requireNonNull(schemaRule);
//        return (schemaParam, resource) -> {
//            final ValidationResult schemaResult = schemaRule.validate((SchemaParam) schemaParam);
//
//            return resourceRule == null ? schemaResult : schemaResult.merge(resourceRule.validate(resource));
//        };
//    }

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
