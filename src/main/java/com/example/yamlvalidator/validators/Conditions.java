package com.example.yamlvalidator.validators;

import com.example.yamlvalidator.ValidatorUtils;
import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.StringParameter;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static com.example.yamlvalidator.ValidatorUtils.*;

public interface Conditions extends Predicate<StringParameter> {

    Predicate<StringParameter> isNumber = ValidatorUtils::canBeParsedToInt;
    Predicate<StringParameter> isNAN = isNumber.negate();
    Predicate<ObjectParameter> hasDuplicates = p -> p.getDuplicates().size() > 0;
    BiPredicate<StringParameter, StringParameter> compareNums = (min, max) -> toInt(min) > toInt(max);
    BiPredicate<List<StringParameter>, StringParameter> contains = List::contains;

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
