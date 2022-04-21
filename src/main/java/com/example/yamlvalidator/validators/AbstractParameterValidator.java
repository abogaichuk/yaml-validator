package com.example.yamlvalidator.validators;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.StringParameter;
import com.example.yamlvalidator.entity.ValidationError;

import java.util.Optional;

import static com.example.yamlvalidator.ValidatorUtils.isEmpty;
import static com.example.yamlvalidator.ValidatorUtils.isNotEmpty;

public abstract class AbstractParameterValidator {
    protected final String DEFAULT = "Default";
    protected final String TYPE = "Type";
    protected final String FORMAT = "Format";
    protected final String VALIDATOR = "Validators";
    protected static final String MIN = "Min";
    protected static final String MAX = "Max";

    protected Optional<ValidationError> canBeParsedToInt(StringParameter intParam, String error) {
        String value = getValue(intParam);
        try {
            int i = Integer.parseInt(value);
            return Optional.empty();
        } catch (NumberFormatException e) {
            return Optional.of(ValidationError.of(error, intParam.getName(), intParam.getColumn()));
        }
    }

    protected Optional<? extends Parameter> findChild(String name, ObjectParameter parameter) {
        return isNotEmpty(name) ? parameter.getChildren().stream()
            .filter(param -> name.equals(param.getName()))
            .findAny() : Optional.empty();
    }

    protected Optional<? extends Parameter> findValidatorByName(String name, ObjectParameter parameter) {
        return isEmpty(name) ? Optional.empty() :
            findChild(VALIDATOR, parameter)
                .flatMap(p -> findChild(name, (ObjectParameter) p));
    }

    protected String getValue(Parameter p) {
        return ((StringParameter) p).getValue();
    }
}
