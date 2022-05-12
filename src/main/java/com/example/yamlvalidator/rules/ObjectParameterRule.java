package com.example.yamlvalidator.rules;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.ValidationResult;

import static com.example.yamlvalidator.entity.ValidationResult.invalid;
import static com.example.yamlvalidator.entity.ValidationResult.valid;
import static com.example.yamlvalidator.utils.ValidatorUtils.*;
import static com.example.yamlvalidator.validators.Conditions.hasDuplicates;

public class ObjectParameterRule implements Rule {
    @Override
    public ValidationResult validate(ObjectParameter parameter) {
//        parameter.findChild(VALIDATOR)
//                .map(ObjectParameter.class::cast)
//                .map(ObjectParameter::getChildren)
//                .orElseGet(Collections::emptyList);
//        return null;
        if ("number".equals(parameter.getTypeChildValue())) {
            return noDuplicates().validate(parameter);
        } else if ("string".equals(parameter.getTypeChildValue())) {
            return noDuplicates().validate(parameter);
        } else if ("datetime".equals(parameter.getTypeChildValue())) {
            return noDuplicates().validate(parameter);
        } else {
            return noDuplicates().validate(parameter);
        }
    }

    private static Rule noDuplicates() {
        return parameter -> hasDuplicates.test(parameter) ? invalid(toErrorMessage(parameter, HAS_DUPLICATES)) : valid();
    }
}
