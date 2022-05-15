package com.example.yamlvalidator.rules;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.StringParameter;
import com.example.yamlvalidator.entity.ValidationResult;

import java.util.Arrays;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static com.example.yamlvalidator.entity.ValidationResult.invalid;
import static com.example.yamlvalidator.entity.ValidationResult.valid;
import static com.example.yamlvalidator.rules.Conditions.*;
import static com.example.yamlvalidator.rules.PadmGrammar.*;
import static com.example.yamlvalidator.utils.ValidatorUtils.*;

public class ParameterRuleFactory {

    public static ParameterRule<ObjectParameter> objectRules() {
//        return bypass()
//                .or(custom().and(numbers()).and(datetime()).and(strings()).and(secrets()).and(booleans()));
        return bypass()
                .or(custom().and(standardTypeRule()));
    }

    private static ParameterRule<ObjectParameter> standardTypeRule() {
        return parameter -> parameter.findChild(KeyWord.TYPE.name())
                .filter(type -> type instanceof StringParameter)
                .map(StringParameter.class::cast)
                .map(type -> StandardType.valueOf(type.getValue().toUpperCase()))
                .map(standardType -> standardType.validate(parameter))
                .orElseGet(ValidationResult::valid);
    }

    public static ParameterRule<StringParameter> stringRules() {
        return correctType();//.and(keyWordRule());
    }

    private static ParameterRule<StringParameter> correctType() {
        return parameter -> notKeywordAndUnknownType.test(parameter) ?
                invalid(toErrorMessage(parameter, UNKNOWN_TYPE)) : valid();
    }

    private static ParameterRule<ObjectParameter> bypass() {
        return singleFieldValidation(KeyWord.BYPASS.name(), PARAMETER_BYPASS, isByPass);
    }

    public static ParameterRule<ObjectParameter> custom() {
        return noDuplicates()
                .and(keyWordRule())
                .and(children())
                .and(singleFieldValidation(KeyWord.TYPE.name(), UNKNOWN_TYPE, hasUnknownType)); //type child is defined correctly
    }

    private static ParameterRule<ObjectParameter> noDuplicates() {
        return parameter -> hasDuplicates.test(parameter) ? invalid(toErrorMessage(parameter, HAS_DUPLICATES)) : valid();
    }

    private static ParameterRule<ObjectParameter> children() {
        return parameter -> parameter.getChildren().stream()
                .map(Parameter::validate)
                .reduce(ValidationResult::merge)
                .orElseGet(ValidationResult::valid);
    }

    private static ParameterRule<ObjectParameter> keyWordRule() {
        return parameter -> parameter.getChildren().stream()
                .map(p -> isKeyWordAndIncorrectType.test(p) ? invalid(toErrorMessage(p, WRONG_KEYWORD)) : valid())
                .reduce(ValidationResult::merge)
                .orElseGet(ValidationResult::valid);
    }

    //todo datetime custom pattern
    public static ParameterRule<ObjectParameter> datetime() {
        return singleFieldValidation(KeyWord.AFTER.name(), IS_NOT_A_DATETIME, isDateTime.negate())
                .and(singleFieldValidation(KeyWord.BEFORE.name(), IS_NOT_A_DATETIME, isDateTime.negate()))
                .and(singleFieldValidation(KeyWord.DEFAULT.name(), IS_NOT_A_DATETIME, isDateTime.negate()))
                .and(doubleFieldsValidation(KeyWord.AFTER.name(), KeyWord.BEFORE.name(), IS_BEFORE, compareDates))
                .and(doubleFieldsValidation(KeyWord.BEFORE.name(), KeyWord.DEFAULT.name(), IS_AFTER, compareDates.negate()))
                .and(doubleFieldsValidation(KeyWord.AFTER.name(), KeyWord.DEFAULT.name(), IS_BEFORE, compareDates));
    }

    public static ParameterRule<ObjectParameter> numbers() {
        return singleFieldValidation(KeyWord.MIN.name(), IS_NAN, isNAN)
                .and(singleFieldValidation(KeyWord.MAX.name(), IS_NAN, isNAN))
                .and(singleFieldValidation(KeyWord.DEFAULT.name(), IS_NAN, isNAN))
                .and(doubleFieldsValidation(KeyWord.MIN.name(), KeyWord.MAX.name(), LESS_THAN, compareNums))
                .and(doubleFieldsValidation(KeyWord.MIN.name(), KeyWord.DEFAULT.name(), LESS_THAN, compareNums))
                .and(doubleFieldsValidation(KeyWord.MAX.name(), KeyWord.DEFAULT.name(), MORE_THAN, compareNums.negate()))
                .and(doubleFieldsValidation(KeyWord.LIST.name(), KeyWord.DEFAULT.name(), DEFAULT_WRONG, listContains.negate()));
    }

    public static ParameterRule<ObjectParameter> booleans() {
        return singleFieldValidation(KeyWord.DEFAULT.name(), IS_NOT_A_BOOLEAN, isBoolean);
    }

    public static ParameterRule<ObjectParameter> secrets() {
        return parameter -> ValidationResult.valid();
    }

    public static ParameterRule<ObjectParameter> strings() {
        return doubleFieldsValidation(KeyWord.LIST.name(), KeyWord.DEFAULT.name(), DEFAULT_WRONG, listContains.negate());
    }

    private static ParameterRule<ObjectParameter> singleFieldValidation(String child, String message, Predicate<Parameter> predicate) {
        return parameter -> parameter.findChild(child)
                .filter(predicate)
                .map(p -> invalid(toErrorMessage(p, message)))
                .orElseGet(ValidationResult::valid);
    }

    private static ParameterRule<ObjectParameter> doubleFieldsValidation(String child1, String child2, String message,
                                               BiPredicate<Parameter, Parameter> comparator) {
        return parameter ->  parameter.findChild(child1)
                .map(p1 -> parameter.findChild(child2)
                        .filter(p2 -> comparator.test(p1, p2))
                        .map(p2 -> invalid(toErrorMessage(p2, p1, message)))
                        .orElseGet(ValidationResult::valid))
                .orElseGet(ValidationResult::valid);
    }
}
