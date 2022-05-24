package com.example.yamlvalidator.rules;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.StringParameter;
import com.example.yamlvalidator.utils.ValidatorUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static com.example.yamlvalidator.utils.ValidatorUtils.*;

public final class Conditions {
    private Conditions() {}

    private static final Predicate<Parameter> isNumber = parameter -> toInt(parameter).isPresent();
    static final Predicate<Parameter> isNAN = isNumber.negate();
    static final Predicate<Parameter> isDateTime = parameter -> toDatetime(parameter).isPresent();
    static final Predicate<Parameter> isBoolean = parameter -> toBoolean(parameter).isPresent();

    static final Predicate<Parameter> isByPass = p -> toBoolean(p).filter(Boolean.TRUE::equals).isPresent();

    static final Predicate<ObjectParameter> hasDuplicates = p -> p.isNotASequenceType() && p.containsDuplicates();
    static final Predicate<StringParameter> isWrongTypeDefinition = p -> p.isTypeOrNotAKeyword() && p.isWrongType();

    static final BiPredicate<Parameter, Parameter> compareNums = (min, max) ->
            compare(min, max, ValidatorUtils::toInt, (a, b) -> a > b);
    static final BiPredicate<Parameter, Parameter> compareDates = (before, after) ->
            compare(before, after, ValidatorUtils::toDatetime, LocalDateTime::isAfter);
    static final BiPredicate<Parameter, Parameter> listContains = (list, value) ->
            contains((ObjectParameter) list, value, ValidatorUtils::toList, ValidatorUtils::toString, List::contains);
    static final BiPredicate<Parameter, Parameter> toDateTime = (pattern, parameter) ->
            toDatetime(pattern, parameter).isPresent();

    static final BiPredicate<ObjectParameter, Parameter> mandatoryParam = (schema, resource) ->
            schema.isRequired() && resource == null;
}
