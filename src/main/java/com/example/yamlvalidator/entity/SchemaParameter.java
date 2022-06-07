package com.example.yamlvalidator.entity;

import com.example.yamlvalidator.grammar.RuleService;
import com.example.yamlvalidator.grammar.StandardType;

//public class SchemaParameter extends Parameter {
//    public SchemaParameter(String name, String path, Object value, Position position) {
//        super();.of(name, path, value, position);
//    }

//    public ValidationResult validate(RuleService rules, Resource resource) {
//        ValidationResult self = getType().ruleFunction.apply(rules).validate(this, resource);
//        return getChildren().stream()
//                .map(SchemaParam.class::cast)
//                .map(param -> param.validate(rules, getAppropriateResource(param.getName(), resource)))
//                .reduce(self, ValidationResult::merge);
//    }
//
//    protected Resource getAppropriateResource(String name, Param resource) {
//        return resource == null ? null : resource.getChildren().stream()
//                .filter(child -> name.equalsIgnoreCase(child.getName()))
//                .map(Resource.class::cast)
//                .findAny().orElse(null);
//    }
//
//    public StandardType getType() {
//        return StandardType.getOrDefault(getTypeValue());
//    }
//}
