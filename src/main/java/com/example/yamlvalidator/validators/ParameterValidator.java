package com.example.yamlvalidator.validators;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.StringParameter;
import com.example.yamlvalidator.entity.ValidationResult;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.example.yamlvalidator.ValidatorUtils.DEFAULT;
import static com.example.yamlvalidator.ValidatorUtils.DEFAULT_LESS_THAN_MIN;
import static com.example.yamlvalidator.ValidatorUtils.DEFAULT_MORE_THAN_MAX;
import static com.example.yamlvalidator.ValidatorUtils.DEFAULT_WRONG;
import static com.example.yamlvalidator.ValidatorUtils.HAS_DUPLICATES;
import static com.example.yamlvalidator.ValidatorUtils.MAX;
import static com.example.yamlvalidator.ValidatorUtils.MAX_IS_NAN;
import static com.example.yamlvalidator.ValidatorUtils.MAX_LESS_THAN_MIN;
import static com.example.yamlvalidator.ValidatorUtils.MIN;
import static com.example.yamlvalidator.ValidatorUtils.MIN_IS_NAN;
import static com.example.yamlvalidator.ValidatorUtils.VALIDATOR_LIST;
import static com.example.yamlvalidator.ValidatorUtils.VALIDATOR_MAX;
import static com.example.yamlvalidator.ValidatorUtils.VALIDATOR_MIN;
import static com.example.yamlvalidator.ValidatorUtils.toErrorMessage;
import static com.example.yamlvalidator.entity.ValidationResult.invalid;
import static com.example.yamlvalidator.entity.ValidationResult.valid;

public interface ParameterValidator extends Function<ObjectParameter, ValidationResult> {

    //todo validators for objectParams?
    //todo duplicates
//    Set<? extends Parameter> duplicates = p.getChildren()
//        .stream()
//        .filter(parameter -> Collections.frequency(p.getChildren(), parameter) > 1)
//        .collect(Collectors.toSet());
    ParameterValidator minNotNAN = of(Conditions.isNAN, VALIDATOR_MIN, MIN_IS_NAN);
    ParameterValidator maxNotNAN = of(Conditions.isNAN, VALIDATOR_MAX, MAX_IS_NAN);
    ParameterValidator maxNotLessThanMin = of(Conditions.compareNums, VALIDATOR_MIN, MAX, MAX_LESS_THAN_MIN);
    ParameterValidator defaultLessThanMin = of(Conditions.compareNums, VALIDATOR_MIN, DEFAULT, DEFAULT_LESS_THAN_MIN);
    ParameterValidator defaultMoreThanMax = of(Conditions.compareNums.negate(), VALIDATOR_MAX, DEFAULT, DEFAULT_MORE_THAN_MAX);

    ParameterValidator numbers = minNotNAN.and(maxNotNAN).and(maxNotLessThanMin)
        .and(maxNotLessThanMin).and(defaultLessThanMin).and(defaultMoreThanMax);

    ParameterValidator defaultInList = list(Conditions.contains.negate(), VALIDATOR_LIST, DEFAULT, DEFAULT_WRONG);

    ParameterValidator noDuplicates = object(Conditions.hasDuplicates, HAS_DUPLICATES);

    static ParameterValidator object(final Predicate<ObjectParameter> predicate,
                                     final String message) {
        return parameter -> predicate.test(parameter) ? invalid(toErrorMessage(parameter, message)) : valid();
    }

    static ParameterValidator of(final Predicate<StringParameter> predicate,
                                 final String path,
                                 final String message) {
        return parameter ->  parameter.getChild(path)
            .filter(predicate)
            .map(p -> invalid(toErrorMessage(p, message)))
            .orElseGet(ValidationResult::valid);
    }

    static ParameterValidator of(final BiPredicate<StringParameter, StringParameter> predicate,
                                 final String path1, final String path2, final String message) {
        return parameter -> parameter.getChild(path1)
            .map(p1 -> parameter.getChild(path2)
                .filter(p2 -> predicate.test(p1, p2))
                .map(p2 -> invalid(toErrorMessage(p2, message)))
                .orElseGet(ValidationResult::valid))
            .orElseGet(ValidationResult::valid);
    }

    static ParameterValidator list(final BiPredicate<List<StringParameter>, StringParameter> predicate,
                                 final String path1, final String path2, final String message) {
        return parameter -> parameter.findValidatorParam(path1)
            .filter(p1 -> p1 instanceof ObjectParameter)
            .map(ObjectParameter.class::cast)
            .map(p1 -> p1.getChildren().stream()
                .filter(sp -> sp instanceof StringParameter)
                .map(StringParameter.class::cast)
                .collect(Collectors.toList()))
            .map(list -> parameter.getChild(path2)
                .filter(p2 -> predicate.test(list, p2))
                .map(p2 -> invalid(toErrorMessage(p2, message)))
                .orElseGet(ValidationResult::valid))
            .orElseGet(ValidationResult::valid);
    }

    default ParameterValidator and(final ParameterValidator other) {
        return parameter -> {
            final ValidationResult left = this.apply(parameter);
            final ValidationResult right = other.apply(parameter);

            return left.merge(right);
        };
    }
}
