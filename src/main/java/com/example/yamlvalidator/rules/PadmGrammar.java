package com.example.yamlvalidator.rules;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.StringParameter;
import com.example.yamlvalidator.entity.ValidationResult;
import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;

import static com.example.yamlvalidator.entity.ValidationResult.invalid;
import static com.example.yamlvalidator.entity.ValidationResult.valid;
import static com.example.yamlvalidator.rules.Conditions.*;
import static com.example.yamlvalidator.rules.PadmGrammar.KeyWord.*;
import static com.example.yamlvalidator.rules.ParameterRuleHelper.doubleFieldsValidation;
import static com.example.yamlvalidator.rules.ParameterRuleHelper.singleFieldValidation;
import static com.example.yamlvalidator.utils.ValidatorUtils.*;

public final class PadmGrammar {
    public static final String OR_TYPE_SPLITTER = " or ";

    private PadmGrammar() {}

    public static ParameterRule<StringParameter> correctType() {
        return parameter -> isWrongTypeDefinition.test(parameter)
                ? invalid(toErrorMessage(parameter, parameter.getValue(), UNKNOWN_TYPE)) : valid();
    }

    //keywords have specific type value: oneOf is object(mapping node), description is string(scalar node)
    public static ParameterRule<? extends Parameter> keyWordRule() {
        return parameter -> parameter.getKeyWord()
                .map(keyWord -> keyWord.getParamType().validate(parameter))
                .orElseGet(ValidationResult::valid);
    }

    public enum StandardType implements ParameterRule<ObjectParameter> {
        OBJECT(objects()),
        STRING(strings()),
        DATETIME(datetime()),
        NUMBER(numbers()),
        BOOLEAN(booleans()),
        SECRET(secrets());

        private final ParameterRule<ObjectParameter> rule;
        StandardType(ParameterRule<ObjectParameter> rule) {
            this.rule = rule;
        }

        public static Optional<StandardType> get(String name) {
            return Arrays.stream(StandardType.values())
                    .filter(value -> value.name().equalsIgnoreCase(name))
                    .findAny();
        }

        @Override
        public ValidationResult validate(ObjectParameter parameter) {
            return rule.validate(parameter);
        }

        private static ParameterRule<ObjectParameter> objects() {
            //if bypass == skip validation
            //if keyword has incorrect type, does not make sense to proceed validation into parameter
            return bypass()
                    .or(noDuplicates()
                            .and((ParameterRule<ObjectParameter>) keyWordRule())
                            .or(children()));
//                            .or(standardTypeRule()));
        }

        private static ParameterRule<ObjectParameter> bypass() {
            return singleFieldValidation(KeyWord.BYPASS.name(), PARAMETER_BYPASS, isByPass);
        }

        private static ParameterRule<ObjectParameter> noDuplicates() {
            return parameter -> hasDuplicates.test(parameter)
                    ? invalid(toErrorMessage(parameter, HAS_DUPLICATES)) : valid();
        }

        private static ParameterRule<ObjectParameter> children() {
            return parameter -> parameter.getChildren().stream()
                    .map(Parameter::validate)
                    .reduce(ValidationResult::merge)
                    .orElseGet(ValidationResult::valid);
        }

        private static ParameterRule<ObjectParameter> datetime() {
            return objects()
                    .and(incorrectDatetimePatternRules()
                            .or(comparingDatesRule()));
        }

        //todo datetime custom pattern or default pattern value?
        private static ParameterRule<ObjectParameter> incorrectDatetimePatternRules() {
            return singleFieldValidation(AFTER.name(), IS_NOT_A_DATETIME, isDateTime.negate())
                    .and(singleFieldValidation(BEFORE.name(), IS_NOT_A_DATETIME, isDateTime.negate()))
                    .and(singleFieldValidation(DEFAULT.name(), IS_NOT_A_DATETIME, isDateTime.negate()));
//            return doubleFieldsValidation(PATTERN.name(), DEFAULT.name(), DATETIME_PARSED_ERROR, toDateTime);
        }

        private static ParameterRule<ObjectParameter> comparingDatesRule() {
            return doubleFieldsValidation(AFTER.name(), BEFORE.name(), IS_BEFORE, compareDates)
                    .and(doubleFieldsValidation(BEFORE.name(), DEFAULT.name(), IS_AFTER, compareDates.negate()))
                    .and(doubleFieldsValidation(AFTER.name(), DEFAULT.name(), IS_BEFORE, compareDates));
        }

        private static ParameterRule<ObjectParameter> numbers() {
            return objects().and(isNotNan()
                    .or(comparingNumbersRule())
                    .and(listContainDefaultRule()));
        }

        private static ParameterRule<ObjectParameter> isNotNan() {
            return singleFieldValidation(MIN.name(), IS_NAN, isNAN)
                    .and(singleFieldValidation(MAX.name(), IS_NAN, isNAN))
                    .and(singleFieldValidation(DEFAULT.name(), IS_NAN, isNAN));
        }

        private static ParameterRule<ObjectParameter> comparingNumbersRule() {
            return doubleFieldsValidation(MIN.name(), MAX.name(), LESS_THAN, compareNums)
                    .and(doubleFieldsValidation(MIN.name(), DEFAULT.name(), LESS_THAN, compareNums))
                    .and(doubleFieldsValidation(MAX.name(), DEFAULT.name(), MORE_THAN, compareNums.negate()));
        }

        //todo listRule almost the same with oneOf or anyOf rule
        private static ParameterRule<ObjectParameter> listContainDefaultRule() {
            return doubleFieldsValidation(LIST.name(), DEFAULT.name(), DEFAULT_WRONG, listContains.negate());
        }

        private static ParameterRule<ObjectParameter> booleans() {
            return objects()
                    .and(singleFieldValidation(DEFAULT.name(), IS_NOT_A_BOOLEAN, isBoolean.negate()));
        }

        //todo secret validation?
        private static ParameterRule<ObjectParameter> secrets() {
            return parameter -> valid();
        }

        private static ParameterRule<ObjectParameter> strings() {
            return objects()
                    .and(listContainDefaultRule());
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
