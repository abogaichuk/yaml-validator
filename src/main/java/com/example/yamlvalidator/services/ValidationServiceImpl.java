package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.*;
import com.example.yamlvalidator.factory.ChildRule;
import com.example.yamlvalidator.factory.ComparingChildRule;
import com.example.yamlvalidator.factory.Rule;
import com.example.yamlvalidator.validators.Conditions;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.yamlvalidator.entity.ValidationResult.invalid;
import static com.example.yamlvalidator.entity.ValidationResult.valid;
import static com.example.yamlvalidator.utils.ValidatorUtils.*;
import static com.example.yamlvalidator.utils.ValidatorUtils.MIN_IS_NAN;
import static com.example.yamlvalidator.validators.Conditions.*;
import static com.example.yamlvalidator.validators.ParameterValidationRules.getRulesFor;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

public class ValidationServiceImpl implements ValidationService {
    @Override
    public ValidationResult validate(Definition definition) {
//        List<ValidationResult> results = extractObjectParams(definition.getChildren())
//            .map(getRulesFor())
//            .peek(result -> result.getReasons().forEach(System.out::println))
//            .collect(Collectors.toList());
//        results.forEach(ValidationResult::getReasons);
//        Visitor v = new ParameterVisitor();
//        definition.getParameters().forEach(parameter -> parameter.accept(v));
//        List<ValidationResult> results = definition.getParameters().stream()
//                .map(parameter -> parameter.accept(v))
//                .peek(result -> result.getReasons().forEach(System.out::println))
//                .collect(Collectors.toList());

//        ValidationResult result = definition.getParameters().stream()
//                .filter(parameter -> parameter instanceof ObjectParameter)
//                .map(ObjectParameter.class::cast)
//                .map(parameter -> Validator.of().validate(parameter))
//                .filter(validationResult -> !validationResult.isValid())
//                .reduce(ValidationResult::merge)
//                .orElseGet(ValidationResult::valid);
//        ValidationResult result = definition.getParameters().stream()
//                .map(Parameter::validate)
//                .reduce(ValidationResult::merge)
//                .orElseGet(ValidationResult::valid);
        ValidationResult result = definition.validate();
        result.getReasons().forEach(System.out::println);
        return ValidationResult.valid();
    }

    private Stream<ObjectParameter> extractObjectParams(List<Parameter> parameters) {
        return parameters.stream()
                .filter(parameter -> parameter instanceof ObjectParameter)
                .map(ObjectParameter.class::cast)
                .flatMap(parameter -> {
                    Stream<ObjectParameter> objectParameters;
                    if (parameter.getChildren().isEmpty()) {
                        objectParameters = of(parameter);
                    } else {
                        objectParameters = concat(of(parameter), extractObjectParams(parameter.getChildren()));
                    }
                    return objectParameters;
                });
    }

    private Stream<StringParameter> extractStringParams(List<? extends Parameter> parameters) {
        return parameters.stream()
                .flatMap(parameter -> {
                    if (parameter instanceof ObjectParameter) {
                        return extractStringParams(((ObjectParameter)parameter).getChildren());
                    } else {
                        return of(parameter);
                    }
                })
                .map(StringParameter.class::cast);
    }

//    private List<ValidationResult> process(List<? extends Parameter> parameters) {
//        parameters.stream()
//                .map(this::test)
//                .map()
//    }
//
//    private ValidationResult test(Parameter parameter) {
//        if (parameter instanceof ObjectParameter)
//    }
}
