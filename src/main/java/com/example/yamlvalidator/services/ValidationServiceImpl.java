package com.example.yamlvalidator.services;

import com.example.yamlvalidator.ValidatorUtils;
import com.example.yamlvalidator.entity.*;
import com.example.yamlvalidator.validators.ParameterValidator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.yamlvalidator.validators.ParameterValidator.*;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

public class ValidationServiceImpl implements ValidationService {
    @Override
    public ValidationResult validate(Definition definition) {
//        extractObjectParams(definition.getParameters())
//                .map(noDuplicates)
//                .filter(result -> !result.isValid())
//                .forEach(result -> System.out.println(result.getReasons()));
//        extractObjectParams(definition.getParameters())
//                .map(pa)
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
