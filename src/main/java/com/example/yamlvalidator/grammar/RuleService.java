package com.example.yamlvalidator.grammar;

import com.example.yamlvalidator.entity.Param;
import com.example.yamlvalidator.entity.ValidationResult;
import com.example.yamlvalidator.services.MessageProvider;
import com.example.yamlvalidator.utils.ValidatorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.example.yamlvalidator.entity.ValidationResult.invalid;
import static com.example.yamlvalidator.entity.ValidationResult.valid;
import static com.example.yamlvalidator.grammar.Conditions.*;
import static com.example.yamlvalidator.grammar.KeyWord.*;
import static com.example.yamlvalidator.utils.ValidatorUtils.*;

@Component
public class RuleService {
    @Autowired
    private MessageProvider messageProvider;

    ValidationRule objects() {
        return bypass()
                .or(noDuplicates().and(mandatoryParameterRule()));
    }

    ValidationRule customs() {
        //todo all scalar params here and custom params without type child
        return objects().or(hasCorrectTypeRule());
    }

    private ValidationRule hasCorrectTypeRule() {
        return (schema, resource) -> schemaTypeRule().validate(schema).merge(resourceTypeRule(schema, resource));
    }

    private ValidationResult resourceTypeRule(Param schema, Param resource) {
        if (resource == null)
            return valid();
        if (isNotEmpty(schema.getName()) && (KeyWord.TYPE.name().equalsIgnoreCase(schema.getName()) || schema.isNotAKeyword())) {

            String typeValue = schema.getTypeValue();
            if (isNotEmpty(typeValue)) {
                boolean match = Stream.of(schema.getTypeValue().split(OR_TYPE_SPLITTER))
                        .map(String::trim)
                        .filter(ValidatorUtils::isNotEmpty)
                        .anyMatch(possibleType -> {
                            if (StandardType.NUMBER.name().equalsIgnoreCase(possibleType))
                                return toInt(resource).isPresent();
                            else if (StandardType.BOOLEAN.name().equalsIgnoreCase(possibleType)) {
                                return toBoolean(resource).isPresent();
                            } else
                                return true;
                            //todo else if(isCustomType("ManualTest"))
                        });
                return match ? valid() : invalid(messageProvider.getMessage(MESSAGE_RESOURCE_UNKNOWN_TYPE, resource, resource.getValue(), typeValue));
            }
        }
        return ValidationResult.valid();
    }

    private ParameterRule schemaTypeRule() {
        return schema -> schema.findIncorrectTypeValue()
                        .map(s -> invalid(messageProvider.getMessage(MESSAGE_UNKNOWN_TYPE, schema, s)))
                        .orElseGet(ValidationResult::valid);
    }

    ValidationRule datetime() {
        return objects()
                .or(validateDatetimeFields()
                        .or(compareDatetimeFields()));
    }

    ValidationRule numbers() {
        return objects()
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
        return objects().or(listContainsRule());
    }

    private ValidationRule bypass() {
        return (schema, resource) -> schema.findChild(BYPASS.name())
                .filter(boolValueIsTrue)
                .map(p -> invalid(messageProvider.getMessage(MESSAGE_PARAMETER_BYPASS, schema.getName(), p)))
                .orElseGet(ValidationResult::valid);
    }

    private ValidationRule noDuplicates() {
        return ValidationRule.of(hasDuplicates());
    }

    private ParameterRule hasDuplicates() {
        return parameter -> {
            var duplicates = parameter.getDuplicates();
            return duplicates.isEmpty() || Param.YamlType.SEQUENCE.equals(parameter.getYamlType()) ? valid() : invalid(
                    messageProvider.getMessage(
                            MESSAGE_HAS_DUPLICATES,
                            parameter,
                            duplicates.stream()
                                    .map(Param::getPath)
                                    .findFirst().get())
            );
        };
    }

    private ValidationRule mandatoryParameterRule() {
        return (schema, resource) -> {
            if (schema.isMandatory() && !schema.hasDefaultValue()) {
                if (resource == null || isEmpty(resource.getValue()))
                    return ValidationResult.invalid(messageProvider.getMessage(MANDATORY_PARAMETER, schema));
            }
            return ValidationResult.valid();
        };
    }

//    private ValidationRule uniqueParameterRule() {
//        return (schema, resource) -> {
////            return isBoolean.test(schema.findChild(UNIQUE.name()))
////                    ? valid()
////                    : invalid();
//            return schema.findChild(UNIQUE.name())
//                    .map(param -> isBoolean.negate().test(param))
//                    .map(invalid(messageProvider.getMessage()))
//        };
//    }

    private ValidationRule compareDatetimeFields() {
        return (schema, resource) -> compareSchemaParams(AFTER, BEFORE, isLeftAfterRight, MESSAGE_IS_BEFORE)
                .and(compareSchemaParams(BEFORE, DEFAULT, isLeftAfterRight.negate(), MESSAGE_IS_AFTER))
                .and(compareSchemaParams(AFTER, DEFAULT, isLeftAfterRight, MESSAGE_IS_BEFORE))
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
                                        BiPredicate<Param, Param> comparator, String message) {
        return param -> param.findChild(child1.name())
                .map(p -> comparison(p, child2, comparator, message).validate(param))
                .orElseGet(ValidationResult::valid);
    }

    private ParameterRule comparison(Param src, KeyWord child,
                                     BiPredicate<Param, Param> comparator, String message) {
        return schema -> schema.findChild(child.name())
                .filter(threshold -> src != null && comparator.test(threshold, src))
                .map(threshold -> invalid(messageProvider.getMessage(message, src, threshold)))
                .orElseGet(ValidationResult::valid);
    }

    private ParameterRule validationRule(Predicate<Param> condition, String errorMessage) {
        return param -> condition.test(param)
                ? invalid(messageProvider.getMessage(errorMessage, param))
                : valid();
    }

    private ParameterRule groupValidationRule(Predicate<Param> condition, String errorMessage, KeyWord ... children) {
        return param -> Stream.of(children)
                .map(KeyWord::name)
                .map(param::findChild)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(condition)
                .map(child -> invalid(messageProvider.getMessage(errorMessage, child)))
                .reduce(ValidationResult.valid(), ValidationResult::merge);
    }
}
