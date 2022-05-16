package com.example.yamlvalidator.rules;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.ValidationResult;

import java.util.function.Predicate;

import static com.example.yamlvalidator.entity.ValidationResult.invalid;
import static com.example.yamlvalidator.entity.ValidationResult.valid;
import static com.example.yamlvalidator.rules.Conditions.*;
import static com.example.yamlvalidator.rules.ParameterRuleFactory.doubleFieldsValidation;
import static com.example.yamlvalidator.rules.ParameterRuleFactory.singleFieldValidation;
import static com.example.yamlvalidator.utils.ValidatorUtils.*;

public class PadmGrammar {
    public static final String OR_TYPE_SPLITTER = " or ";

    public enum StandardType implements ParameterRule<ObjectParameter> {
        OBJECT(secrets()),
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
            return singleFieldValidation(KeyWord.AFTER.name(), IS_NOT_A_DATETIME, isDateTime.negate())
                    .and(singleFieldValidation(KeyWord.BEFORE.name(), IS_NOT_A_DATETIME, isDateTime.negate()))
                    .and(singleFieldValidation(KeyWord.DEFAULT.name(), IS_NOT_A_DATETIME, isDateTime.negate()))
                    .and(doubleFieldsValidation(KeyWord.AFTER.name(), KeyWord.BEFORE.name(), IS_BEFORE, compareDates))
                    .and(doubleFieldsValidation(KeyWord.BEFORE.name(), KeyWord.DEFAULT.name(), IS_AFTER, compareDates.negate()))
                    .and(doubleFieldsValidation(KeyWord.AFTER.name(), KeyWord.DEFAULT.name(), IS_BEFORE, compareDates));
        }

        private static ParameterRule<ObjectParameter> numbers() {
            return singleFieldValidation(KeyWord.MIN.name(), IS_NAN, isNAN)
                    .and(singleFieldValidation(KeyWord.MAX.name(), IS_NAN, isNAN))
                    .and(singleFieldValidation(KeyWord.DEFAULT.name(), IS_NAN, isNAN))
                    .and(doubleFieldsValidation(KeyWord.MIN.name(), KeyWord.MAX.name(), LESS_THAN, compareNums))
                    .and(doubleFieldsValidation(KeyWord.MIN.name(), KeyWord.DEFAULT.name(), LESS_THAN, compareNums))
                    .and(doubleFieldsValidation(KeyWord.MAX.name(), KeyWord.DEFAULT.name(), MORE_THAN, compareNums.negate()))
                    .and(doubleFieldsValidation(KeyWord.LIST.name(), KeyWord.DEFAULT.name(), DEFAULT_WRONG, listContains.negate()));
        }

        private static ParameterRule<ObjectParameter> booleans() {
            return singleFieldValidation(KeyWord.DEFAULT.name(), IS_NOT_A_BOOLEAN, isBoolean);
        }

        private static ParameterRule<ObjectParameter> secrets() {
            return parameter -> valid();
        }

        private static ParameterRule<ObjectParameter> strings() {
            return doubleFieldsValidation(KeyWord.LIST.name(), KeyWord.DEFAULT.name(), DEFAULT_WRONG, listContains.negate());
        }
    }

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

        public final KeyWordType paramType;
        KeyWord(KeyWordType paramType) {
            this.paramType = paramType;
        }
    }

    public enum KeyWordType implements ParameterRule<Parameter> {
        STRING(STRING_KEYWORD, isStringParameter),
        OBJECT(OBJECT_KEYWORD, isObjectParameter);

        public final String message;
        public final Predicate<Parameter> predicate;
        KeyWordType(String message, Predicate<Parameter> predicate) {
            this.message = message;
            this.predicate = predicate;
        }

        @Override
        public ValidationResult validate(Parameter parameter) {
            return predicate.negate().test(parameter) ? invalid(toErrorMessage(parameter, message)) : valid();
        }
    }
}
