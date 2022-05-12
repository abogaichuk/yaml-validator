//package com.example.yamlvalidator.factory;
//
//import com.example.yamlvalidator.entity.ObjectParameter;
//import com.example.yamlvalidator.entity.Parameter;
//import com.example.yamlvalidator.entity.ValidationResult;
//
//import java.util.function.Predicate;
//
//import static com.example.yamlvalidator.entity.ValidationResult.invalid;
//import static com.example.yamlvalidator.entity.ValidationResult.valid;
//import static com.example.yamlvalidator.utils.ValidatorUtils.toErrorMessage;
//
//public class CustomRule implements Rule {
//    private final String message;
//    private final Predicate<ObjectParameter> predicate;
//
//    public CustomRule(Predicate<ObjectParameter> predicate, String message) {
//        this.predicate = predicate;
//        this.message = message;
//    }
//
//    @Override
//    public ValidationResult validate(Parameter parameter) {
////        return parameter -> predicate.test(parameter) ? invalid(toErrorMessage(parameter, message)) : valid();
//        return predicate.test((ObjectParameter) parameter) ? invalid(toErrorMessage(parameter, message)) : valid();
//    }
//}
