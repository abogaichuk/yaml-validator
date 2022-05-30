package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.Resource;
import com.example.yamlvalidator.entity.Schema;
import com.example.yamlvalidator.entity.ValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ValidationServiceImpl implements ValidationService {
    @Autowired
    private MessageProvider messageProvider;
    Stack<Number> stack = new Stack<>();

    @Override
    public ValidationResult validate(Schema schema, List<Resource> resources) {
        ValidationResult schemaValidationResult = schema.validate();
        ValidationResult result = schema.validateResources(resources)
                .reduce(schemaValidationResult, ValidationResult::merge);
//        ValidationResult result = resources.stream()
//                .map(schema::validate)
//                .reduce(schemaResult, ValidationResult::merge);


        return result;
    }

    private void push(Collection<? extends Number> numbers) {
        stack.addAll(numbers);
    }

    private void pop(Collection<Number> objcts) {
        objcts.add(stack.pop());
    }
}
