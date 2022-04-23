package com.example.yamlvalidator.services;

import com.example.yamlvalidator.ValidatorUtils;
import com.example.yamlvalidator.entity.Definition;
import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.ValidationResult;
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

import static com.example.yamlvalidator.validators.ParameterValidator.*;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toCollection;

public class ValidationServiceImpl implements ValidationService {
    @Override
    public ValidationResult validate(Definition definition) {
//        Optional<Parameter> port = definition.getParameters().stream()
//            .filter(parameter -> "Port".equalsIgnoreCase(parameter.getName()))
//            .findAny();
//        if (port.isPresent()) {
//            return numbers.apply((ObjectParameter) port.get());
//        }
        definition.getParameters().stream()
            .filter(parameter -> "Protocol".equalsIgnoreCase(parameter.getName()))
            .findAny()
            .ifPresent(protocol -> {
                ObjectParameter p = (ObjectParameter) protocol;
                Set<String> reasons = defaultInList.apply(p).getReasons();
                reasons.forEach(System.out::println);
            });
        return ValidationResult.valid();
//        List<? extends Parameter> validators = definition.getParameters().stream()
//            .filter(parameter -> parameter instanceof ObjectParameter)
//            .map(ObjectParameter.class::cast)
//            .map(parameter -> parameter.findChildR("Validators/List"))
//            .filter(Optional::isPresent)
//            .map(Optional::get).collect(Collectors.toList());
//        return ValidationResult.valid();
    }
}
