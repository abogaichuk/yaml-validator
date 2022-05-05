package com.example.yamlvalidator.factory;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.StringParameter;
import com.example.yamlvalidator.entity.ValidationResult;

import java.util.function.Predicate;

import static com.example.yamlvalidator.entity.ValidationResult.invalid;
import static com.example.yamlvalidator.utils.ValidatorUtils.*;

public class ChildRule implements Rule {
    private final String message;
    private final String childName;
    private final Predicate<StringParameter> predicate;

    public ChildRule(String childName, String message, Predicate<StringParameter> predicate) {
        this.childName = childName;
        this.message = message;
        this.predicate = predicate;
    }

    @Override
    public ValidationResult validate(ObjectParameter parameter) {
        return parameter.getChildAsString(childName)
                .filter(predicate)
                .map(p -> invalid(toErrorMessage(p, message)))
                .orElseGet(ValidationResult::valid);
    }
}
