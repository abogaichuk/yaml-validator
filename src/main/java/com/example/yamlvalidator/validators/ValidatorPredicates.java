package com.example.yamlvalidator.validators;

import com.example.yamlvalidator.entity.ObjectParameter;

import java.util.function.Predicate;

public interface ValidatorPredicates extends Predicate<ObjectParameter> {
//    Predicate<ObjectParameter> isObj = parameter -> parameter != null && parameter instanceof ObjectParameter;
    Predicate<ObjectParameter> isNumber = parameter -> parameter.getTypeFieldValue().equals("number");
    Predicate<ObjectParameter> isString = parameter -> parameter.getTypeFieldValue().equals("string");
    Predicate<ObjectParameter> isDateTime = parameter -> parameter.getTypeFieldValue().equals("datetime");
//    Predicate<ObjectParameter> containsValidator = parameter -> parameter.findChild("Validators").isPresent();


    Predicate<ObjectParameter> isMin = parameter -> parameter.getChildAsString("Min").isPresent();
    Predicate<ObjectParameter> isMax = parameter -> parameter.getChildAsString("Max").isPresent();
    Predicate<ObjectParameter> isList = parameter -> parameter.getChildAsString("List").isPresent();
    Predicate<ObjectParameter> all = isMin.and(isMax).and(isList);

    Predicate<ObjectParameter> exist = parameter -> parameter.findChild("Validators").isPresent();
}
