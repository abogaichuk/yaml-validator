package com.example.yamlvalidator.grammar;

import com.example.yamlvalidator.entity.Param;
import com.example.yamlvalidator.utils.ValidatorUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static com.example.yamlvalidator.utils.ValidatorUtils.*;

public final class Conditions {
    private Conditions() {}

    private static final Predicate<Param> isNumber = parameter -> toInt(parameter).isPresent();
    public static final Predicate<Param> isNAN = isNumber.negate();
    static final Predicate<Param> isDateTime = parameter -> toDatetime(parameter).isPresent();
    static final Predicate<Param> isBoolean = parameter -> toBoolean(parameter).isPresent();

//    static final Predicate<Param> isByPass = p -> toBoolean(p).filter(Boolean.TRUE::equals).isPresent();
    public static final Predicate<Param> boolValueIsTrue = p -> toBoolean(p).filter(Boolean.TRUE::equals).isPresent();

//    static final Predicate<Param> hasDuplicates = p -> p.isNotASequenceType() && p.containsDuplicates();
//    static final Predicate<Param> isWrongTypeDefinition = p -> p.isTypeOrNotAKeyword() && p.isWrongType();

    static final BiPredicate<Param, Param> compareNums = (min, max) ->
            compare(min, max, ValidatorUtils::toInt, (a, b) -> a > b);
    static final BiPredicate<Param, Param> isLeftAfterRight = (left, right) ->
            compare(left, right, ValidatorUtils::toDatetime, LocalDateTime::isAfter);
    static final BiPredicate<Param, Param> listContains = (list, value) ->
            contains(list, value, ValidatorUtils::toList, ValidatorUtils::toString, List::contains);
    static final BiPredicate<Param, Param> toDateTime = (pattern, parameter) ->
            toDatetime(pattern, parameter).isPresent();

//    static final BiPredicate<Param, Param> mandatoryParam = (schema, resource) ->
//            schema.isRequired() && resource == null;
}
