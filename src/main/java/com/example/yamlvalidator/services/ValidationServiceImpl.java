package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.*;
import com.example.yamlvalidator.factory.RulesFactory;
import com.example.yamlvalidator.rules.Rule;
import com.example.yamlvalidator.rules.datetime.AfterIsBeforeBeforeRule;
import com.example.yamlvalidator.rules.datetime.AfterValidatorRule;
import com.example.yamlvalidator.rules.datetime.BeforeValidatorRule;
import com.example.yamlvalidator.rules.default_param.DefaultInListRule;
import com.example.yamlvalidator.rules.default_param.DefaultLessThanMinRule;
import com.example.yamlvalidator.rules.default_param.DefaultMoreThanMaxRule;
import com.example.yamlvalidator.rules.numbers.MaxLessThanMinRule;
import com.example.yamlvalidator.rules.numbers.MaxValidatorRule;
import com.example.yamlvalidator.rules.numbers.MinValidatorRule;

import java.util.List;
import java.util.stream.Stream;

import static com.example.yamlvalidator.entity.ValidationResult.invalid;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

public class ValidationServiceImpl implements ValidationService {
    @Override
    public ValidationResult validate(final Definition definition) {
//        List<Rule> rules = getAllRules();
//        ValidationResult result = extractObjectParams(definition.getChildren())
//                .map(parameter -> rules.stream()
//                        .map(rule -> rule.validate(parameter))
//                        .reduce(ValidationResult::merge)
//                        .orElseGet(ValidationResult::valid))
//                .reduce(ValidationResult::merge)
//                .orElseGet(ValidationResult::valid);
//        result.getReasons().forEach(System.out::println);

        ValidationResult result = definition.getChildren().stream()
                .filter(parameter -> parameter instanceof ObjectParameter)
                .map(ObjectParameter.class::cast)
                .map(parameter -> RulesFactory.getRules(parameter).validate(parameter))
                .reduce(ValidationResult::merge)
                .orElseGet(ValidationResult::valid);
        result.getReasons().forEach(System.out::println);
        return ValidationResult.valid();
    }

//    public ValidationResult secretValidators(ObjectParameter parameter) {
//        System.out.println("secret!!");
//        return ValidationResult.valid();
//    }
//
//    public ValidationResult datetimeValidators(ObjectParameter parameter) {
//        System.out.println("datetime!");
//        return ValidationResult.valid();
//    }
//
//    public ValidationResult numberValidators(ObjectParameter parameter) {
//        System.out.println("number!");
//        return ValidationResult.valid();
//    }
//
//    public ValidationResult booleanValidators(ObjectParameter parameter) {
//        System.out.println("boolean!");
//        return ValidationResult.valid();
//    }
//
//    public ValidationResult objectValidators(ObjectParameter parameter) {
//        System.out.println("object!");
//        return ValidationResult.valid();
//    }
//
//    public ValidationResult stringValidators(ObjectParameter parameter) {
//        System.out.println("string!");
//        return ValidationResult.valid();
//    }

    private List<Rule> getAllRules() {
        return List.of(new AfterIsBeforeBeforeRule(), new AfterValidatorRule(), new BeforeValidatorRule(),
                new DefaultInListRule(), new DefaultLessThanMinRule(), new DefaultMoreThanMaxRule(),
                new MaxLessThanMinRule(), new MaxValidatorRule(), new MinValidatorRule());
    }

    private Stream<Parameter> extractAll(List<Parameter> parameters) {
        return parameters.stream()
                .flatMap(parameter -> {
                    if (parameter instanceof ObjectParameter) {
                        return Stream.concat(Stream.of(parameter), extractAll(((ObjectParameter) parameter).getChildren()));
                    }
                    return Stream.of(parameter);
                });
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
