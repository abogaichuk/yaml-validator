package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.Definition;
import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.StringParameter;
import com.example.yamlvalidator.entity.ValidationResult;
import com.example.yamlvalidator.validators.ParameterValidationRules;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.yamlvalidator.validators.ParameterValidationRules.*;
import static com.example.yamlvalidator.validators.ParameterValidationRules.getRulesFor;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

public class ValidationServiceImpl implements ValidationService {
    @Override
    public ValidationResult validate(Definition definition) {
        List<ValidationResult> results = extractObjectParams(definition.getParameters())
            .map(getRulesFor())
//            .map(parameter -> ParameterValidationRules.getRulesFor().apply(parameter))
            .peek(result -> result.getReasons().forEach(System.out::println))
            .collect(Collectors.toList());
        results.forEach(ValidationResult::getReasons);
        return ValidationResult.valid();
    }

    private Stream<ObjectParameter> extractObjectParams(List<? extends Parameter> parameters) {
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
