//package com.example.yamlvalidator.validators;
//
//import com.example.yamlvalidator.utils.ValidatorUtils;
//import com.example.yamlvalidator.entity.ObjectParameter;
//import com.example.yamlvalidator.entity.StringParameter;
//import com.example.yamlvalidator.entity.ValidationResult;
//
//import java.util.function.BiFunction;
//import java.util.function.Function;
//import java.util.function.Predicate;
//
//import static com.example.yamlvalidator.utils.ValidatorUtils.*;
//import static com.example.yamlvalidator.entity.ValidationResult.invalid;
//import static com.example.yamlvalidator.entity.ValidationResult.valid;
//
//public interface Validator extends Function<ObjectParameter, ValidationResult> {
////    Validator empty = $ -> valid();
//
////    Validator isMinANumber = rule(ParameterPredicate.minIsNumber, "Validator.Min is not an integer");
//
////    static Validator rule(final Predicate<StringParameter> predicate, final String message) {
////        return parameter -> predicate.test(parameter) ? invalid(toErrorMessage(parameter, message)) : valid();
////    }
//
////    Validator isMinANumber = of(ParameterPredicate.isNumber, "MIN", "Validator.Min is not an integer");
////    Validator isMaxANumber = of(ParameterPredicate.isNumber, "MAX", "Validator.Max is not an integer");
////
////    static Validator of(final Predicate<StringParameter> condition,
////                        final String validatorName, final String message) {
////        return parameter -> findValidatorByName(validatorName, parameter)
////            .map(StringParameter.class::cast)
////            .filter(stringParameter -> !condition.test(stringParameter))
////            .map(stringParameter -> invalid(toErrorMessage(stringParameter, message)))
////            .orElseGet(ValidationResult::valid);
////    }
////
////    default Validator and(final Validator other) {
////        return user ->
////        {
////            final ValidationResult left = this.apply(user);
////            final ValidationResult right = other.apply(user);
////
////            return left.merge(right);
////        };
////    }
//}
