package com.example.yamlvalidator.rules;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.ValidationResult;

import java.util.List;

public interface Validatable {
    ValidationResult validate(List<Parameter> resources);
}
