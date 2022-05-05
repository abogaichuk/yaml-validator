package com.example.yamlvalidator.validators;

import com.example.yamlvalidator.entity.ObjectParameter;

import java.util.function.Predicate;

public interface ValidatorPredicates extends Predicate<ObjectParameter> {
    Predicate<ObjectParameter> isCustom = parameter -> parameter.getTypeChildValue().equals("custom");
    Predicate<ObjectParameter> isNumber = parameter -> parameter.getTypeChildValue().equals("number");
    Predicate<ObjectParameter> isString = parameter -> parameter.getTypeChildValue().equals("string");
    Predicate<ObjectParameter> isDateTime = parameter -> parameter.getTypeChildValue().equals("datetime");
//    Predicate<ObjectParameter> containsValidator = parameter -> parameter.findChild("Validators").isPresent();


    Predicate<ObjectParameter> isMin = parameter -> parameter.getChildAsString("Min").isPresent();
    Predicate<ObjectParameter> isMax = parameter -> parameter.getChildAsString("Max").isPresent();
    Predicate<ObjectParameter> isList = parameter -> parameter.getChildAsString("List").isPresent();
    Predicate<ObjectParameter> all = isMin.and(isMax).and(isList);

    Predicate<ObjectParameter> exist = parameter -> parameter.findChild("Validators").isPresent();
}
