package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.Definition;
import com.example.yamlvalidator.entity.ValidationResult;

public interface ValidationService {
    ValidationResult validate(Definition definition);
}
