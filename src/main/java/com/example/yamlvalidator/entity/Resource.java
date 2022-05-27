package com.example.yamlvalidator.entity;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Resource extends Param {
//    @Getter
//    private final List<Resource> children = new ArrayList<>();

    public Resource(String name, String value, Param parent, Position position) {
        super(name, value, parent, position);
    }

//    public void addChildren(List<Resource> list) {
//        children.addAll(list);
//    }

//    public ValidationResult validate(Schema schema) {
//        Optional<SchemaParam> schemaParamO = schema.findSchemaParamForResource(this);
//        return ValidationResult.valid();
//    }
}
