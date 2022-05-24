package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.Definition;
import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.ValidationResult;

import java.util.List;

public interface ValidationService {
    ValidationResult validate(ObjectParameter definition, List<Parameter> resources);
}
