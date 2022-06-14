package com.example.yamlvalidator.grammar;

import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.Schema;
import com.example.yamlvalidator.entity.ValidationResult;

import java.util.Collections;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.yamlvalidator.entity.ValidationResult.invalid;
import static com.example.yamlvalidator.entity.ValidationResult.valid;
import static com.example.yamlvalidator.grammar.Conditions.*;
import static com.example.yamlvalidator.grammar.KeyWord.*;
import static com.example.yamlvalidator.utils.MessagesUtils.*;
import static com.example.yamlvalidator.utils.ValidatorUtils.isEmpty;

public class RuleService {

    ValidationRule objects() {
        return common()
                .and(oneOfRule())
                .and((schema, resource) -> valid());
    }

    ValidationRule datetime() {
        return common()
                .or(standardTypeRule())
                .or(validateDatetimeFields()
                        .or(compareDatetimeFields()));
    }

    ValidationRule numbers() {
        return common()
                .or(standardTypeRule())
                .or(validateNumberFields())
                .or(compareNumberFields()
                        .and(listContainsRule()));
    }

    ValidationRule booleans() {
        return ValidationRule.of(
                schema -> groupValidationRule(isBoolean.negate(), MESSAGE_IS_NOT_A_BOOLEAN, DEFAULT)
                        .validate(schema),
                resource -> validationRule(isBoolean.negate(), MESSAGE_IS_NOT_A_BOOLEAN)
                        .validate(resource)
        );
    }

    ValidationRule strings() {
        return common()
                .or(standardTypeRule())
                .or(listContainsRule());
    }

    ValidationRule common() {
        return bypass().or(noDuplicates()
//                .and(mandatoryParameter())
        );
    }

    private ValidationRule standardTypeRule() {
        return (schema, resource) -> {
            var customFields = schema.findCustomFields()
                    .map(Parameter::getName)
                    .collect(Collectors.toList());
            return customFields.size() > 0
                    ? invalid(getMessage(MESSAGE_SCHEMA_INCORRECT, schema, customFields))
                    : valid();
        };
    }

    private ValidationRule bypass() {
        return (schema, resource) -> schema.findChild(BYPASS.name())
                .filter(boolValueIsTrue)
                .map(p -> invalid(getMessage(MESSAGE_PARAMETER_BYPASS, schema.getName(), p)))
                .orElseGet(ValidationResult::valid);
    }

    private ValidationRule noDuplicates() {
        return ValidationRule.of(hasDuplicates());
    }

    private ParameterRule hasDuplicates() {
        return parameter -> {
            var duplicates = parameter.getDuplicates();
            return duplicates.isEmpty() || Parameter.YamlType.SEQUENCE.equals(parameter.getType()) ? valid() : invalid(
                    getMessage(
                            MESSAGE_HAS_DUPLICATES,
                            parameter,
                            duplicates.stream()
                                    .map(Parameter::getPath)
                                    .findFirst().get())
            );
        };
    }

    private ValidationRule oneOfRule() {
        var service = this;
        return (schema, resource) -> {
            var validationResults = schema.findChild("oneOf")
                    .map(oneOf -> oneOf.getChildren()
                            .map(Schema.class::cast)
                            .map(schemaParam -> schemaParam.validate(service, resource))
                            .collect(Collectors.toList()))
                    .orElseGet(Collections::emptyList);

            return validationResults.isEmpty() || validationResults.stream().anyMatch(ValidationResult::isValid)
                    ? valid()
                    : invalid(getMessage(MESSAGE_INVALID_RESOURCE, resource));
        };
    }

//    private ValidationRule mandatoryParameter() {
//        var service = this;
//        return (schema, resource) -> {
//            if ((schema.isMandatory() || KeyWord.ONEOF.name().equalsIgnoreCase(schema.getParentName())) && !schema.hasDefaultValue()) {
//                if (resource == null) {
//                    if (schema.getParent().isMandatory() || schema.getParent().getParent() == null) {
//                        return invalid(getMessage(MANDATORY_PARAMETER, schema));
//                    } else {
//                        return valid();
//                    }
//                } else {
//                    var customFields = schema.findCustomFieldNames();
//                    var resourceFields = resource.findCustomFieldNames();
//                    if (customFields.size() > 0) {
//                        //at least one child must be present
//                        if (!Collections.disjoint(customFields, resourceFields)) {
//                            return valid();
//                        } else {
//                            return invalid(getMessage(MANDATORY_CUSTOM_CHILDREN, schema, customFields));
//                        }
//                    } else {
//                        if (isEmpty(resource.getValue()) && resourceFields.isEmpty()) {
//                            return invalid(getMessage(MANDATORY_PARAMETER, schema));
//                        }
//                    }
//                }
//            }
//            return valid();
//        };
//    }

//    private ValidationRule uniqueParameterRule() {
//        return (schema, resource) -> {
////            return isBoolean.test(schema.findChild(UNIQUE.name()))
////                    ? valid()
////                    : invalid();
//            return schema.findChild(UNIQUE.name())
//                    .map(param -> isBoolean.negate().test(param))
//                    .map(invalid(getMessage()))
//        };
//    }

    private ValidationRule compareDatetimeFields() {
        return (schema, resource) -> compareSchemaParams(BEFORE, AFTER, isLeftAfterRight, MESSAGE_IS_BEFORE)
                .and(compareSchemaParams(BEFORE, DEFAULT, isLeftAfterRight, MESSAGE_IS_AFTER))
                .and(compareSchemaParams(DEFAULT, AFTER, isLeftAfterRight, MESSAGE_IS_BEFORE))
                .or(comparison(resource, AFTER, isLeftAfterRight.negate(), MESSAGE_IS_AFTER)
                        .and(comparison(resource, BEFORE, isLeftAfterRight, MESSAGE_IS_BEFORE)))
                .validate(schema);
    }

    private ValidationRule validateDatetimeFields() {
        return ValidationRule.of(
                schema -> groupValidationRule(isDateTime.negate(), MESSAGE_IS_NOT_A_DATETIME,
                        AFTER, BEFORE, DEFAULT, EXAMPLE).validate(schema),
                resource -> validationRule(isDateTime.negate(), MESSAGE_IS_NOT_A_DATETIME).validate(resource)
        );
    }

    private ValidationRule validateNumberFields() {
        return ValidationRule.of(
                schema -> groupValidationRule(isNAN, MESSAGE_IS_NAN, MAX, MIN, DEFAULT, EXAMPLE)
                        .validate(schema),
                resource -> validationRule(isNAN, MESSAGE_IS_NAN).validate(resource)
        );
    }

    private ValidationRule compareNumberFields() {
        return (schema, resource) -> compareSchemaParams(MAX, MIN, compareNums, MESSAGE_LESS_THAN)
                .and(compareSchemaParams(DEFAULT, MIN, compareNums, MESSAGE_LESS_THAN))
                .and(compareSchemaParams(DEFAULT, MAX, compareNums.negate(), MESSAGE_MORE_THAN))
                .or(comparison(resource, MIN, compareNums, MESSAGE_LESS_THAN)
                        .and(comparison(resource, MAX, compareNums.negate(), MESSAGE_MORE_THAN)))
                .validate(schema);
    }

    private ValidationRule listContainsRule() {
        return (schema, resource) ->
                compareSchemaParams(DEFAULT, LIST, listContains.negate(), MESSAGE_LIST_DOES_NOT_CONTAIN)
                        .or(comparison(resource, LIST, listContains.negate(), MESSAGE_LIST_DOES_NOT_CONTAIN))
                        .validate(schema);
    }

    private ParameterRule compareSchemaParams(KeyWord child1, KeyWord child2,
                                        BiPredicate<Parameter, Parameter> comparator, String message) {
        return param -> param.findChild(child1.name())
                .map(p -> comparison(p, child2, comparator, message).validate(param))
                .orElseGet(ValidationResult::valid);
    }

    private ParameterRule comparison(Parameter src, KeyWord child,
                                     BiPredicate<Parameter, Parameter> comparator, String message) {
        return schema -> schema.findChild(child.name())
                .filter(threshold -> src != null && comparator.test(threshold, src))
                .map(threshold -> invalid(getMessage(message, src, threshold)))
                .orElseGet(ValidationResult::valid);
    }

    private ParameterRule validationRule(Predicate<Parameter> condition, String errorMessage) {
        return param -> condition.test(param)
                ? invalid(getMessage(errorMessage, param))
                : valid();
    }

    private ParameterRule groupValidationRule(Predicate<Parameter> condition, String errorMessage, KeyWord ... children) {
        return param -> Stream.of(children)
                .map(KeyWord::name)
                .map(param::findChild)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(condition)
                .map(child -> invalid(getMessage(errorMessage, child)))
                .reduce(valid(), ValidationResult::merge);
    }
}
