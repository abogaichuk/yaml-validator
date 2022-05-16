package com.example.yamlvalidator.rules;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.StringParameter;
import com.example.yamlvalidator.entity.ValidationResult;
import lombok.Getter;

import java.util.function.Predicate;

import static com.example.yamlvalidator.entity.ValidationResult.invalid;
import static com.example.yamlvalidator.entity.ValidationResult.valid;
import static com.example.yamlvalidator.rules.Conditions.*;
import static com.example.yamlvalidator.rules.PadmGrammar.KeyWord.*;
import static com.example.yamlvalidator.rules.ParameterRuleFactory.doubleFieldsValidation;
import static com.example.yamlvalidator.rules.ParameterRuleFactory.singleFieldValidation;
import static com.example.yamlvalidator.utils.ValidatorUtils.*;

public final class PadmGrammar {
    public static final String OR_TYPE_SPLITTER = " or ";

    private PadmGrammar() {}

    public enum StandardType implements ParameterRule<ObjectParameter> {
        OBJECT(p -> valid()),
        STRING(strings()),
        DATETIME(datetime()),
        NUMBER(numbers()),
        BOOLEAN(booleans()),
        SECRET(secrets());

        private final ParameterRule<ObjectParameter> rule;
        StandardType(ParameterRule<ObjectParameter> rule) {
            this.rule = rule;
        }

        @Override
        public ValidationResult validate(ObjectParameter parameter) {
            return rule.validate(parameter);
        }

        //todo datetime custom pattern
        private static ParameterRule<ObjectParameter> datetime() {
            return singleFieldValidation(AFTER.name(), IS_NOT_A_DATETIME, isDateTime.negate())
                    .and(singleFieldValidation(BEFORE.name(), IS_NOT_A_DATETIME, isDateTime.negate()))
                    .and(singleFieldValidation(DEFAULT.name(), IS_NOT_A_DATETIME, isDateTime.negate()))
                    .and(doubleFieldsValidation(AFTER.name(), BEFORE.name(), IS_BEFORE, compareDates))
                    .and(doubleFieldsValidation(BEFORE.name(), DEFAULT.name(), IS_AFTER, compareDates.negate()))
                    .and(doubleFieldsValidation(AFTER.name(), DEFAULT.name(), IS_BEFORE, compareDates));
        }

        private static ParameterRule<ObjectParameter> numbers() {
            return singleFieldValidation(MIN.name(), IS_NAN, isNAN)
                    .and(singleFieldValidation(MAX.name(), IS_NAN, isNAN))
                    .and(singleFieldValidation(DEFAULT.name(), IS_NAN, isNAN))
                    .and(doubleFieldsValidation(MIN.name(), MAX.name(), LESS_THAN, compareNums))
                    .and(doubleFieldsValidation(MIN.name(), DEFAULT.name(), LESS_THAN, compareNums))
                    .and(doubleFieldsValidation(MAX.name(), DEFAULT.name(), MORE_THAN, compareNums.negate()))
                    .and(doubleFieldsValidation(LIST.name(), DEFAULT.name(), DEFAULT_WRONG, listContains.negate()));
        }

        private static ParameterRule<ObjectParameter> booleans() {
            return singleFieldValidation(DEFAULT.name(), IS_NOT_A_BOOLEAN, isBoolean);
        }

        private static ParameterRule<ObjectParameter> secrets() {
            return parameter -> valid();
        }

        private static ParameterRule<ObjectParameter> strings() {
            return doubleFieldsValidation(LIST.name(), DEFAULT.name(), DEFAULT_WRONG, listContains.negate());
        }
    }

    @Getter
    public enum KeyWord {
        TYPE(KeyWordType.STRING),
        ITEMS(KeyWordType.OBJECT),
        ENUM(KeyWordType.OBJECT),
        ONEOF(KeyWordType.OBJECT),
        ANYOF(KeyWordType.OBJECT),
        PROPERTIES(KeyWordType.OBJECT),
        PATTERN(KeyWordType.STRING),
        DESCRIPTION(KeyWordType.STRING),
        DEFAULT(KeyWordType.STRING),
        REQUIRED(KeyWordType.STRING),
        EXAMPLE(KeyWordType.STRING),
        BYPASS(KeyWordType.STRING),
        MIN(KeyWordType.STRING),
        MAX(KeyWordType.STRING),
        LIST(KeyWordType.OBJECT),
        AFTER(KeyWordType.STRING),
        BEFORE(KeyWordType.STRING);

        private final KeyWordType paramType;
        KeyWord(KeyWordType paramType) {
            this.paramType = paramType;
        }
    }

    enum KeyWordType implements ParameterRule<Parameter> {
        STRING(STRING_KEYWORD, StringParameter.class::isInstance),
        OBJECT(OBJECT_KEYWORD, ObjectParameter.class::isInstance);

        private final String message;
        private final Predicate<Parameter> predicate;
        KeyWordType(String message, Predicate<Parameter> predicate) {
            this.message = message;
            this.predicate = predicate;
        }

        @Override
        public ValidationResult validate(Parameter parameter) {
            return predicate.test(parameter) ? valid() : invalid(toErrorMessage(parameter, message));
        }
    }
}
