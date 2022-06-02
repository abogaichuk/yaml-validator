package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.Resource;
import com.example.yamlvalidator.entity.Schema;
import com.example.yamlvalidator.entity.SchemaParam;
import com.example.yamlvalidator.entity.ValidationResult;
import com.example.yamlvalidator.grammar.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.example.yamlvalidator.entity.ValidationResult.invalid;

@Service
public class ValidationServiceImpl implements ValidationService {
    @Autowired
//    private MessageProvider messageProvider;
//    private IsNANRule rule;
    private RuleService rules;

    @Override
    public ValidationResult validate(Schema schema, Resource resource) {
//        ValidationResult schemaValidationResult = schema.validate();
//        ValidationResult result = schema.validateResources(resources)
//                .reduce(schemaValidationResult, ValidationResult::merge);
//
//        return result;
//        return schema.getChildren().stream()
//                .map(param -> isNANRule(KeyWord.DEFAULT).and(isNANRule(KeyWord.MIN)).validate((SchemaParam) param))
//                .reduce(ValidationResult.valid(), ValidationResult::merge);
//        return schema.getChildren().stream()
//                .map(SchemaParam.class::cast)
//                .map(rule::validate).reduce(ValidationResult.valid(), ValidationResult::merge);
//        return schema.getChildren().stream()
//                .map(SchemaParam.class::cast)
//                .map(param -> rules.validate(param, resources))
//                .reduce(ValidationResult.valid(), ValidationResult::merge);
        return schema.validate(rules, resource);
    }

//    private Resource getAppropriateResource(String name, List<Resource> resources) {
//        return resources.stream()
//                .filter(resource -> name.equalsIgnoreCase(resource.getName()))
//                .findAny().orElse(null);
//    }

//    private SchemaRule isNANRule(KeyWord keyWord) {
//        return param -> param.findChild(keyWord.name())
//                .filter(Conditions.isNAN)
//                .map(p -> invalid(messageProvider.getMessage(IS_NAN, p.getName(), p.getPath(), p.getRow())))
//                .orElseGet(ValidationResult::valid);
//    }
}
