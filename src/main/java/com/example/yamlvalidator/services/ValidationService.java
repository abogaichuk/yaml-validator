package com.example.yamlvalidator.services;


import com.example.yamlvalidator.entity.Definition;
import com.example.yamlvalidator.entity.ValidationError;

import java.util.List;

public interface ValidationService {
    List<ValidationError> validate(Definition definition);
}
