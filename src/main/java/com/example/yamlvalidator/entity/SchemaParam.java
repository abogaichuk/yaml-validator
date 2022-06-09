package com.example.yamlvalidator.entity;

import com.example.yamlvalidator.grammar.RuleService;
import com.example.yamlvalidator.grammar.StandardType;

import java.util.List;
import java.util.stream.Collectors;

public class SchemaParam extends Param {

    public SchemaParam(String name, String value, Param parent, Position position, YamlType yamlType) {
        super(name, value, parent, position, yamlType);
    }

    public ValidationResult validate(RuleService rules, Resource resource) {
        var result = getType().ruleFunction.apply(rules).validate(this, resource);
//        return findCustomFields()
        return getChildren().stream()
                .filter(Param::isNotAKeyword)
                .map(SchemaParam.class::cast)
                .map(param -> param.validate(rules, getAppropriateResource(param.getName(), resource)))
                .reduce(result, ValidationResult::merge);
    }

//    public List<SchemaParam> getCustomFields() {
//        return getChildren().stream()
//                .map(SchemaParam.class::cast)
//                .filter(Param::isCustomTypeDefinition)
//                .collect(Collectors.toList());
//    }

    public Resource getAppropriateResource(String name, Param resource) {
        return resource == null ? null : resource.getChildren().stream()
                .filter(child -> name.equalsIgnoreCase(child.getName()))
                .map(Resource.class::cast)
                .findAny().orElse(null);
    }

    public StandardType getType() {
        return StandardType.getOrDefault(getTypeValue());
    }
}
