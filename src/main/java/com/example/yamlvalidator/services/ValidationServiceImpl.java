package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.Param;
import com.example.yamlvalidator.entity.Schema;
import com.example.yamlvalidator.entity.ValidationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ValidationServiceImpl implements ValidationService {
    @Autowired
    private MessageProvider messageProvider;

    @Override
    public ValidationResult validate(Schema schema, List<Param> resources) {
        return schema.validate();
//        ValidationResult result = bypass().or(noDuplicates()).validate(schema);
//        return schema.getChildren().stream()
//                .map(child -> bypass().or(noDuplicates()).validate(child))
//                .reduce(result, ValidationResult::merge);
    }
}
