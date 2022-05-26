package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.*;
import com.example.yamlvalidator.grammar.KeyWord;
import com.example.yamlvalidator.grammar.SchemaRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.example.yamlvalidator.entity.ValidationResult.invalid;
import static com.example.yamlvalidator.entity.ValidationResult.valid;
import static com.example.yamlvalidator.utils.ValidatorUtils.*;

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
