package com.example.yamlvalidator.validators;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.ValidationError;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CorrectTypeParameterValidator extends AbstractParameterValidator implements ParameterValidator {
    private final List<String> standardTypes = List.of("number", "string", "object", "datetime", "boolean", "secret");

    @Override
    public Optional<ValidationError> validate(ObjectParameter parameter) {
        Optional<? extends Parameter> type = findChild(TYPE, parameter);
//        return type.isPresent() && !standardTypes.contains(getValue(type.get())) ?
//            Collections.singletonList(ValidationError.of("strange param type", parameter.getName(), parameter.getPosition().getColumn())) :
//            Collections.emptyList();
        return Optional.empty();
    }
}
