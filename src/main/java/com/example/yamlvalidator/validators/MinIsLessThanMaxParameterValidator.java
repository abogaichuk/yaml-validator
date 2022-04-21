package com.example.yamlvalidator.validators;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.ValidationError;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

public class MinIsLessThanMaxParameterValidator extends AbstractParameterValidator implements ParameterValidator {

    @Override
    public Optional<ValidationError> validate(ObjectParameter parameter) {
//        Optional<? extends Parameter> aDefault = findChild(DEFAULT, parameter.getChildren());
//        Optional<? extends Parameter> type = findChild(TYPE, parameter.getChildren());

//        Optional<? extends Parameter> validator = findChild(VALIDATOR, parameter);
//        return validator
//            .map(value -> perform((ObjectParameter) value))
//            .orElse(Collections.emptyList());
        return Optional.empty();
    }

    private List<ValidationError> perform(ObjectParameter validator) {
        Optional<? extends Parameter> minO = findChild(MIN, validator);
        Optional<? extends Parameter> maxO = findChild(MAX, validator);

        List<ValidationError> errors = new ArrayList<>();
        if (minO.isPresent() && maxO.isPresent()) {
            return compare(minO.get(), maxO.get(), "min > max error", this::isMoreThan);
        }
//        if (minO.isPresent() && maxO.isPresent()) {
//            int min = Integer.parseInt(getValue(minO.get()));
//            int max = Integer.parseInt(getValue(maxO.get()));
//            if (min > max) {
//                String message = MessageFormat.format("{0} (row #{1})", "min > max", 1);
//                errors.add(ValidationError.of(message));
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
        return errors;
    }

    private List<ValidationError> compare(Parameter p1, Parameter p2, String message, BiPredicate<Integer, Integer> predicate) {
        List<ValidationError> errors = new ArrayList<>();
        int min = toInt(p1);
        if (min == -1)
            errors.add(ValidationError.of("Validator.Min is not a number", p1.getName(), p1.getPosition().getColumn()));
        int max = toInt(p2);
        if (max == -1)
            errors.add(ValidationError.of("Validator.Max is not a number", p2.getName(), p2.getPosition().getColumn()));
        if (predicate.test(max, min))
            errors.add(ValidationError.of(message, p1.getName(), p1.getPosition().getColumn()));
        return errors;
    }

    private int toInt(Parameter p) {
        try {
            return Integer.parseInt(getValue(p));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

//    private BiPredicate<Integer, Integer> isMoreThan = (max, current) -> current > max;
//    private BiPredicate<Integer, Integer> isLessThan = (max, current) -> current > max;

    private boolean isMoreThan(int max, int current) {
        return current > max;
    }

    private boolean isLessThan(int min, int current) {
        return current < min;
    }

}
