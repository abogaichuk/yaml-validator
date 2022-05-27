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
        ValidationResult schemaResult = schema.validate();
//        ValidationResult result = resources.stream()
//                .map(schema::validate)
//                .reduce(schemaResult, ValidationResult::merge);


//        Stack<String> stack = new Stack<>();
//        stack.add(0xcafebabe);
//        List<Number> longs = new ArrayList<>();
//        longs.add(111L);
//        longs.add(222L);
////        push(longs);
//        pop(longs);
//        longs.forEach(System.out::println);
        return schemaResult;
    }

    private void push(Collection<? extends Number> numbers) {
        stack.addAll(numbers);
    }

    private void pop(Collection<Number> objcts) {
        objcts.add(stack.pop());
    }
}
