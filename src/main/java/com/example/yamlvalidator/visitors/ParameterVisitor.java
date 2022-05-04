//package com.example.yamlvalidator.visitors;
//
//import com.example.yamlvalidator.entity.ObjectParameter;
//import com.example.yamlvalidator.entity.StringParameter;
//import com.example.yamlvalidator.entity.ValidationResult;
//import com.example.yamlvalidator.strategy.Validator;
//import com.example.yamlvalidator.validators.ParameterValidationRules;
//import com.example.yamlvalidator.validators.ValidatorPredicates;
//
//import java.util.function.Function;
//
//import static com.example.yamlvalidator.validators.ParameterValidationRules.*;
//
//public class ParameterVisitor implements Visitor {
//    @Override
//    public ValidationResult visit(StringParameter parameter) {
////        System.out.println("processing String param: " + parameter.getPath());
//        return ValidationResult.valid();
//    }
//
//    @Override
//    public ValidationResult visit(ObjectParameter parameter) {
////        System.out.println("processing Object param: " + parameter.getPath());
//        ValidationResult result;
//        if (ValidatorPredicates.isCustom.test(parameter)) {
//            result = Validator.noDuplicates().validate(parameter);
//        } else if (ValidatorPredicates.isDateTime.test(parameter)) {
//            result = Validator.dateTime().validate(parameter);
//        } else if (ValidatorPredicates.isString.test(parameter)) {
//            result = Validator.strings().and(Validator.noDuplicates()).validate(parameter);
//        } else {
//            result = ValidationResult.valid();
//        }
//        return result;
////        return getRulesFor().apply(parameter);
////        parameter.getChildren().forEach(child -> child.accept(this));
//    }
//}
