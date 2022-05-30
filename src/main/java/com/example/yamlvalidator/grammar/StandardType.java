package com.example.yamlvalidator.grammar;

import com.example.yamlvalidator.entity.Param;
import com.example.yamlvalidator.entity.Resource;
import com.example.yamlvalidator.entity.SchemaParam;
import com.example.yamlvalidator.entity.ValidationResult;
import lombok.Getter;

import java.util.Arrays;

import static com.example.yamlvalidator.entity.ValidationResult.invalid;
import static com.example.yamlvalidator.entity.ValidationResult.valid;
import static com.example.yamlvalidator.grammar.Conditions.*;
import static com.example.yamlvalidator.grammar.KeyWord.*;
import static com.example.yamlvalidator.grammar.RulesBuilder.doubleFieldsValidation;
import static com.example.yamlvalidator.grammar.RulesBuilder.singleFieldValidation;
import static com.example.yamlvalidator.utils.ValidatorUtils.*;

public enum StandardType implements SchemaRule {
    OBJECT(customObject()),
    STRING(strings()),
    DATETIME(datetime()),
    NUMBER(numbers()),
    BOOLEAN(booleans()),
    SECRET(customObject());

    @Getter
    private final SchemaRule rule;
    StandardType(SchemaRule rule) {
        this.rule = rule;
    }

    public static StandardType getOrDefault(String name) {
        return Arrays.stream(StandardType.values())
                .filter(value -> value.name().equalsIgnoreCase(name))
                .findAny().orElse(OBJECT);
    }

    @Override
    public ValidationResult validate(SchemaParam param) {
        return rule.validate(param);
    }

    private static SchemaRule customObject() {
        return bypass().or(noDuplicates());
    }

    private static SchemaRule bypass() {
        return singleFieldValidation(KeyWord.BYPASS.name(), PARAMETER_BYPASS, boolValueIsTrue);
    }

    private static SchemaRule noDuplicates() {
        return param -> {
            var duplicates = param.getDuplicates();
            return duplicates.isEmpty() ? valid() : invalid(
                    toErrorMessage(
                            param,
                            duplicates.stream()
                                    .map(Param::getName)
                                    .findFirst().get(),
                            "Parameter: {0} has duplicates: {1}"));
        };
    }

    private static SchemaRule datetime() {
        return customObject()
                .or(incorrectDatetimePatternRules()
                        .or(comparingDatesRule()));
    }

    //todo datetime custom pattern or default pattern value?
    private static SchemaRule incorrectDatetimePatternRules() {
            return singleFieldValidation(AFTER.name(), IS_NOT_A_DATETIME, isDateTime.negate())
                    .and(singleFieldValidation(BEFORE.name(), IS_NOT_A_DATETIME, isDateTime.negate()))
                    .and(singleFieldValidation(DEFAULT.name(), IS_NOT_A_DATETIME, isDateTime.negate()));
//            return doubleFieldsValidation(PATTERN.name(), DEFAULT.name(), DATETIME_PARSED_ERROR, toDateTime);
    }

    private static SchemaRule comparingDatesRule() {
            return doubleFieldsValidation(AFTER.name(), BEFORE.name(), IS_BEFORE, compareDates)
                    .and(doubleFieldsValidation(BEFORE.name(), DEFAULT.name(), IS_AFTER, compareDates.negate()))
                    .and(doubleFieldsValidation(AFTER.name(), DEFAULT.name(), IS_BEFORE, compareDates));
    }

    private static SchemaRule numbers() {
        return customObject()
                .or(isNotNan()
                        .or(comparingNumbersRule().and(listContainsDefaultRule())));
    }

    private static SchemaRule isNotNan() {
        return singleFieldValidation(MIN.name(), IS_NAN, isNAN)
                .and(singleFieldValidation(MAX.name(), IS_NAN, isNAN))
                .and(singleFieldValidation(DEFAULT.name(), IS_NAN, isNAN))
                .and(singleFieldValidation(EXAMPLE.name(), IS_NAN, isNAN));
    }

    private static SchemaRule comparingNumbersRule() {
        return doubleFieldsValidation(MIN.name(), MAX.name(), LESS_THAN, compareNums)
                .and(doubleFieldsValidation(MIN.name(), DEFAULT.name(), LESS_THAN, compareNums))
                .and(doubleFieldsValidation(MAX.name(), DEFAULT.name(), MORE_THAN, compareNums.negate()));
    }

    private static SchemaRule listContainsDefaultRule() {
        return doubleFieldsValidation(LIST.name(), DEFAULT.name(), DEFAULT_WRONG, listContains.negate());
    }

    private static SchemaRule booleans() {
        return customObject()
                .or(singleFieldValidation(DEFAULT.name(), IS_NOT_A_BOOLEAN, isBoolean.negate()));
    }

    private static SchemaRule strings() {
        return listContainsDefaultRule();
    }
}
