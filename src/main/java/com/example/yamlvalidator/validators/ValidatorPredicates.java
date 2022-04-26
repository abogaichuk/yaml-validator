package com.example.yamlvalidator.validators;

import com.example.yamlvalidator.entity.ObjectParameter;

import java.util.function.Predicate;

public interface ValidatorPredicates extends Predicate<ObjectParameter> {
    Predicate<ObjectParameter> isMin = parameter -> parameter.getChild("Min").isPresent();
    Predicate<ObjectParameter> isMax = parameter -> parameter.getChild("Max").isPresent();
    Predicate<ObjectParameter> isList = parameter -> parameter.getChild("List").isPresent();
    Predicate<ObjectParameter> all = isMin.and(isMax).and(isList);

    Predicate<ObjectParameter> exist = parameter -> parameter.getChild("Validators").isPresent();
}
