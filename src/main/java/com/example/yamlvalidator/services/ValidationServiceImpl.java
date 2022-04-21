package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.Definition;
import com.example.yamlvalidator.entity.ValidationError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ValidationServiceImpl implements ValidationService {
    @Override
    public List<ValidationError> validate(Definition definition) {
        return Collections.emptyList();
    }
//    @Override
//    public List<ValidationError> validate(Definition definition) {
//        return definition.getParameters().stream()
//            .filter(parameter -> !parameter.isBypass())
//            .flatMap(parameter -> checkParameter(parameter).stream())
//            .collect(Collectors.toList());
//    }

//    private List<ValidationError> checkParameter(Parameter parameter) {
//        if (parameter instanceof ObjectParameter) {
//            List<? extends Parameter> children = ((ObjectParameter) parameter).getChildren();
//            Optional<? extends Parameter> aDefault = findChild("Default", children);
//            Optional<? extends Parameter> validator = findChild("Validators", children);
//            if (validator.isPresent()) {
//                return check((ObjectParameter) validator.get(), aDefault);
//            }
//        }
//        return Collections.emptyList();
//    }

//    private List<ValidationError> check(ObjectParameter validator, Optional<? extends Parameter> defaultParam) {
//        List<ValidationError> errors = new ArrayList<>();
//        List<? extends Parameter> children = validator.getChildren();
//        Optional<? extends Parameter> minO = findChild("Min", children);
//        Optional<? extends Parameter> maxO = findChild("Max", children);
//        if (minO.isPresent() && maxO.isPresent()) {
//            int min = Integer.parseInt(getValue(minO.get()));
//            int max = Integer.parseInt(getValue(maxO.get()));
//            if (min > max) {
//                errors.add(ValidationError.of("min > max"));
//            }
//            if (defaultParam.isPresent()) {
//                int defValue = Integer.parseInt(getValue(defaultParam.get()));
//                if (defValue < min) {
//                    errors.add(ValidationError.of("default value < min"));
//                }
//                if (defValue > max) {
//                    errors.add(ValidationError.of("default value > max"));
//                }
//            }
//        } else if (minO.isPresent()) {
//            int min = Integer.parseInt(getValue(minO.get()));
//            if (defaultParam.isPresent()) {
//                int defValue = Integer.parseInt(getValue(defaultParam.get()));
//                if (defValue < min) {
//                    errors.add(ValidationError.of("default value < min"));
//                }
//            }
//        } else if (maxO.isPresent()) {
//            int max = Integer.parseInt(getValue(maxO.get()));
//            if (defaultParam.isPresent()) {
//                int defValue = Integer.parseInt(getValue(defaultParam.get()));
//                if (defValue > max) {
//                    errors.add(ValidationError.of("default value > max"));
//                }
//            }
//        }
//        return errors;
//    }

//    private String getValue(Parameter p) {
//        return ((StringParameter) p).getValue();
//    }

//    private Optional<? extends Parameter> findChild(String name, List<? extends Parameter> children) {
//        return children.stream()
//            .filter(parameter -> name.equals(parameter.getName()))
//            .findAny();
//    }
}
