package com.example.yamlvalidator.rules;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.StringParameter;
import com.example.yamlvalidator.utils.ValidatorUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.example.yamlvalidator.rules.PadmGrammar.KeyWord;
import static com.example.yamlvalidator.utils.ValidatorUtils.*;

public interface Conditions extends Predicate<StringParameter> {

    Predicate<Parameter> isNumber = parameter -> toInt(parameter).isPresent();
    Predicate<Parameter> isNAN = isNumber.negate();
    Predicate<Parameter> isDateTime = parameter -> toDatetime(parameter).isPresent();
    Predicate<Parameter> isBoolean = parameter -> toBoolean(parameter).isPresent();

    Predicate<Parameter> hasUnknownType = p -> KeyWord.TYPE.paramType.predicate.test(p) && isWrongType((StringParameter) p);
    Predicate<Parameter> isKeyWordAndIncorrectType = p -> Stream.of(KeyWord.values())
            .anyMatch(keyWord -> keyWord.name().equalsIgnoreCase(p.getName()) && keyWord.paramType.predicate.negate().test(p));
    Predicate<Parameter> isByPass = p -> toBoolean(p).filter(Boolean.TRUE::equals).isPresent();

    Predicate<ObjectParameter> hasDuplicates = p -> p.isNotASequenceType() && p.containsDuplicates();
    Predicate<StringParameter> notKeywordAndUnknownType = p -> isNotAKeyword(p) && isWrongType(p);

    BiPredicate<Parameter, Parameter> compareNums = (min, max) -> compare(min, max, ValidatorUtils::toInt, (a, b) -> a > b);
    BiPredicate<Parameter, Parameter> compareDates = (before, after) -> compare(before, after, ValidatorUtils::toDatetime, LocalDateTime::isAfter);
    BiPredicate<Parameter, Parameter> listContains = (list, value) -> contains((ObjectParameter) list, value, ValidatorUtils::toList, ValidatorUtils::toString, List::contains);

//    default Conditions negate() {
//        return parameter -> !this.test(parameter);
//    }
//
//    default Conditions and(final Conditions other) {
//        return parameter -> this.test(parameter) && other.test(parameter);
//    }
//
//    default Conditions or(final Conditions other) {
//        return parameter -> this.test(parameter) || other.test(parameter);
//    }
}
