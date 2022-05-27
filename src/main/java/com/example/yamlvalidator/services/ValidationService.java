package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.*;

import java.util.List;

public interface ValidationService {
    ValidationResult validate(Schema schema, List<Resource> resources);
}
