package com.example.yamlvalidator.validators;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.StringParameter;
import com.example.yamlvalidator.entity.ValidationResult;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.example.yamlvalidator.utils.ValidatorUtils.AFTER_IS_NOT_DATETIME;
import static com.example.yamlvalidator.utils.ValidatorUtils.BEFORE_DATE_IS_AFTER;
import static com.example.yamlvalidator.utils.ValidatorUtils.BEFORE_IS_NOT_DATETIME;
import static com.example.yamlvalidator.utils.ValidatorUtils.DEFAULT;
import static com.example.yamlvalidator.utils.ValidatorUtils.DEFAULT_LESS_THAN_MIN;
import static com.example.yamlvalidator.utils.ValidatorUtils.DEFAULT_MORE_THAN_MAX;
import static com.example.yamlvalidator.utils.ValidatorUtils.DEFAULT_WRONG;
import static com.example.yamlvalidator.utils.ValidatorUtils.HAS_DUPLICATES;
import static com.example.yamlvalidator.utils.ValidatorUtils.MAX_IS_NAN;
import static com.example.yamlvalidator.utils.ValidatorUtils.MAX_LESS_THAN_MIN;
import static com.example.yamlvalidator.utils.ValidatorUtils.MIN_IS_NAN;
import static com.example.yamlvalidator.utils.ValidatorUtils.VALIDATOR_AFTER;
import static com.example.yamlvalidator.utils.ValidatorUtils.VALIDATOR_BEFORE;
import static com.example.yamlvalidator.utils.ValidatorUtils.VALIDATOR_LIST;
import static com.example.yamlvalidator.utils.ValidatorUtils.VALIDATOR_MAX;
import static com.example.yamlvalidator.utils.ValidatorUtils.VALIDATOR_MIN;
import static com.example.yamlvalidator.utils.ValidatorUtils.toErrorMessage;
import static com.example.yamlvalidator.entity.ValidationResult.invalid;
import static com.example.yamlvalidator.entity.ValidationResult.valid;

public interface ParameterValidator extends Function<ObjectParameter, ValidationResult> {

    //todo validators for objectParams?
    //todo duplicates for sequence, object sequence?
    //todo secret validation?
    //todo variation group
    //number validators
    ParameterValidator minNotNAN = of(Conditions.isNAN, VALIDATOR_MIN, MIN_IS_NAN);
    ParameterValidator maxNotNAN = of(Conditions.isNAN, VALIDATOR_MAX, MAX_IS_NAN);
    ParameterValidator minGreaterThanMax = of(Conditions.compareNums, VALIDATOR_MIN, VALIDATOR_MAX, MAX_LESS_THAN_MIN);
    ParameterValidator defaultLessThanMin = of(Conditions.compareNums, VALIDATOR_MIN, DEFAULT, DEFAULT_LESS_THAN_MIN);
    ParameterValidator defaultMoreThanMax = of(Conditions.compareNums.negate(), VALIDATOR_MAX, DEFAULT, DEFAULT_MORE_THAN_MAX);

    //list validators
    ParameterValidator defaultInList = list(Conditions.contains.negate(), VALIDATOR_LIST, DEFAULT, DEFAULT_WRONG);

    //datetime validators
    ParameterValidator afterCanBeParsed = of(Conditions.isDateTime.negate(), VALIDATOR_AFTER, AFTER_IS_NOT_DATETIME);
    ParameterValidator beforeCanBeParsed = of(Conditions.isDateTime.negate(), VALIDATOR_BEFORE, BEFORE_IS_NOT_DATETIME);
    ParameterValidator beforeIsAfter = of(Conditions.compareDates, VALIDATOR_BEFORE, VALIDATOR_AFTER, BEFORE_DATE_IS_AFTER);
//    ParameterValidator dateTime = list(Conditions.contains.negate(), VALIDATOR_LIST, DEFAULT, DEFAULT_WRONG);

    //parameter validators
    ParameterValidator noDuplicates = object(Conditions.hasDuplicates, HAS_DUPLICATES);

    ParameterValidator numbers = noDuplicates.and(minNotNAN).and(maxNotNAN).and(minGreaterThanMax)
        .and(defaultLessThanMin).and(defaultMoreThanMax).and(defaultInList);
    ParameterValidator strings = noDuplicates.and(defaultInList);
    ParameterValidator dates = noDuplicates.and(afterCanBeParsed).and(beforeCanBeParsed).and(beforeIsAfter);
    ParameterValidator customObject = noDuplicates;


    static ParameterValidator object(final Predicate<ObjectParameter> predicate,
                                     final String message) {
        return parameter -> predicate.test(parameter) ? invalid(toErrorMessage(parameter, message)) : valid();
    }

    static ParameterValidator of(final Predicate<StringParameter> predicate,
                                 final String path,
                                 final String message) {
        return parameter ->  parameter.getChildAsString(path)
                .filter(predicate)
                .map(p -> invalid(toErrorMessage(p, message)))
                .orElseGet(ValidationResult::valid);
    }

    static ParameterValidator of(final BiPredicate<StringParameter, StringParameter> predicate,
                                 final String path1, final String path2, final String message) {
        return parameter -> parameter.getChildAsString(path1)
                .map(p1 -> parameter.getChildAsString(path2)
                        .filter(p2 -> predicate.test(p1, p2))
                        .map(p2 -> invalid(toErrorMessage(p2, message)))
                        .orElseGet(ValidationResult::valid))
                .orElseGet(ValidationResult::valid);
    }

    static ParameterValidator list(final BiPredicate<List<String>, String> predicate,
                                   final String path1, final String path2, final String message) {
        return parameter -> parameter.findValidatorParam(path1)
                .filter(p1 -> p1 instanceof ObjectParameter)
                .map(ObjectParameter.class::cast)
                .map(p1 -> p1.getChildren().stream()
                        .filter(sp -> sp instanceof StringParameter)
                        .map(StringParameter.class::cast)
                        .map(StringParameter::getValue)
                        .collect(Collectors.toList()))
                .map(list -> parameter.getChildAsString(path2)
                        .filter(p2 -> predicate.test(list, p2.getValue()))
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
