package com.example.yamlvalidator.rules;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.StringParameter;
import com.example.yamlvalidator.entity.ValidationResult;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static com.example.yamlvalidator.entity.ValidationResult.invalid;
import static com.example.yamlvalidator.entity.ValidationResult.valid;
import static com.example.yamlvalidator.rules.Conditions.*;
import static com.example.yamlvalidator.rules.PadmGrammar.KeyWord;
import static com.example.yamlvalidator.rules.PadmGrammar.StandardType;
import static com.example.yamlvalidator.utils.ValidatorUtils.*;

public final class ParameterRuleFactory {

    private ParameterRuleFactory() {}

    public static ParameterRule<ObjectParameter> objectRules() {
        //if bypass == skip validation
        //if keyword has incorrect type, does not make sense to proceed validation into parameter
        return bypass()
                .or(noDuplicates()
                        .and((ParameterRule<ObjectParameter>) keyWordRule())
                        .or(children())
                        .or(standardTypeRule()));
    }

    public static ParameterRule<StringParameter> stringRules() {
        return correctType()
                .and((ParameterRule<StringParameter>) keyWordRule());
    }

    public static ParameterRule<ObjectParameter> enumRules() {
        return children();
    }

    private static ParameterRule<StringParameter> correctType() {
        return parameter -> isWrongTypeDefinition.test(parameter)
                ? invalid(toErrorMessage(parameter, parameter.getValue(), UNKNOWN_TYPE)) : valid();
    }

    private static ParameterRule<ObjectParameter> standardTypeRule() {
        return parameter -> parameter.findChild(KeyWord.TYPE.name())
                .filter(StringParameter.class::isInstance)
                .map(StringParameter.class::cast)
                .map(type -> StandardType.valueOf(type.getValue().toUpperCase()))
                .map(standardType -> standardType.validate(parameter))
                .orElseGet(ValidationResult::valid);
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

    //keywords have specific type value: oneOf is object(mapping node), description is string(scalar node)
    private static ParameterRule<? extends Parameter> keyWordRule() {
        return parameter -> parameter.getKeyWord()
                .map(keyWord -> keyWord.getParamType().validate(parameter))
                .orElseGet(ValidationResult::valid);
    }

    public static ParameterRule<ObjectParameter> singleFieldValidation(String child, String message,
                                                                       Predicate<Parameter> predicate) {
        return parameter -> parameter.findChild(child)
                .filter(predicate)
                .map(p -> invalid(toErrorMessage(p, message)))
                .orElseGet(ValidationResult::valid);
    }

    public static ParameterRule<ObjectParameter> doubleFieldsValidation(String child1, String child2, String message,
                                               BiPredicate<Parameter, Parameter> comparator) {
        return parameter ->  parameter.findChild(child1)
                .map(p1 -> parameter.findChild(child2)
                        .filter(p2 -> comparator.test(p1, p2))
                        .map(p2 -> invalid(toErrorMessage(p2, p1, message)))
                        .orElseGet(ValidationResult::valid))
                .orElseGet(ValidationResult::valid);
    }
}
