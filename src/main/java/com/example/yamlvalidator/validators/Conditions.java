package com.example.yamlvalidator.validators;

import com.example.yamlvalidator.utils.ValidatorUtils;
import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.StringParameter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static com.example.yamlvalidator.utils.ValidatorUtils.*;

public interface Conditions extends Predicate<StringParameter> {

    Predicate<StringParameter> isNumber = parameter -> canBeParsed(parameter, ValidatorUtils::toInt);
    Predicate<StringParameter> isNAN = isNumber.negate();
    Predicate<StringParameter> isDateTime = parameter -> canBeParsed(parameter, ValidatorUtils::toDatetime);
//    Predicate<ObjectParameter> isNumber = parameter -> ValidatorUtils.canBeParsed(parameter, ValidatorUtils::toInt);
//    Predicate<ObjectParameter> isNAN = isNumber.negate();
//    Predicate<ObjectParameter> isDateTime = parameter -> ValidatorUtils.canBeParsed(parameter, ValidatorUtils::toDatetime);
    Predicate<ObjectParameter> hasDuplicates = parameter -> parameter.isNotASequenceType() && parameter.containsDuplicates();
    Predicate<ObjectParameter> isBypass = parameter -> parameter.findChild("bypass")
            .map(StringParameter.class::cast)
            .map(StringParameter::getValue)
            .map(Boolean::parseBoolean)
            .orElse(Boolean.FALSE);
//    BiPredicate<StringParameter, StringParameter> compareNums = ValidatorUtils::compareInts;
    BiPredicate<StringParameter, StringParameter> compareNums = (min, max) -> compare(min, max, ValidatorUtils::toInt, (a, b) -> a > b);
    BiPredicate<StringParameter, StringParameter> compareDates = (before, after) -> compare(before, after, ValidatorUtils::toDatetime, LocalDateTime::isAfter);
//    BiPredicate<StringParameter, StringParameter> compareDates = ValidatorUtils::compareDates;
    BiPredicate<List<String>, String> contains = List::contains;
    BiPredicate<List<StringParameter>, StringParameter> insideList = List::contains;
//    BiPredicate<List<StringParameter>, StringParameter> contains = List::contains;

//    Condition isNumber = ValidatorUtils::canBeParsedToInt;
//    Condition NAN = isNumber.negate();

//    default Condition negate() {
//        return parameter -> !this.test(parameter);
//    }
//
//    default Condition and(final Condition other) {
//        return parameter -> this.test(parameter) && other.test(parameter);
//    }
//
//    default Condition or(final Condition other) {
//        return parameter -> this.test(parameter) || other.test(parameter);
//    }
}
