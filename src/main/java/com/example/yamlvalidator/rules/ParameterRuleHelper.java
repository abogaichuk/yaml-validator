package com.example.yamlvalidator.rules;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.StringParameter;
import com.example.yamlvalidator.entity.ValidationResult;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static com.example.yamlvalidator.entity.ValidationResult.invalid;
import static com.example.yamlvalidator.entity.ValidationResult.valid;
import static com.example.yamlvalidator.utils.ValidatorUtils.MANDATORY_PARAMETER;
import static com.example.yamlvalidator.utils.ValidatorUtils.toErrorMessage;

public final class ParameterRuleHelper {

    private ParameterRuleHelper() {}

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

    public static ValidationRule<ObjectParameter, Parameter> mandatoryResource() {
        return (definition, resource) -> Conditions.mandatoryParam.test(definition, resource) ?
                invalid(toErrorMessage(definition, MANDATORY_PARAMETER)) :valid();
    }

    //todo how to check resource type
    //todo usage after validation? final result?
//    public static ValidationRule<StringParameter, Parameter> resourceHasRightType() {
//        return (definition, resource) ->
//    }
}
