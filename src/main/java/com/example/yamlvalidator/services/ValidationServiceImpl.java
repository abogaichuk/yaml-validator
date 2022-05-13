package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.Definition;
import com.example.yamlvalidator.entity.ValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ValidationServiceImpl implements ValidationService {
    @Autowired
    private MessageProvider messageProvider;

    @Override
    public ValidationResult validate(final Definition definition) {
        ValidationResult result = definition.validate();
        result.getReasons().forEach(System.out::println);
        return ValidationResult.valid();
    }
}
