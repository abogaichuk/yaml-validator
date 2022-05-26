package com.example.yamlvalidator.entity;

import java.util.List;
import java.util.stream.Collectors;

public class Schema extends SchemaParam {
    public Schema(String name, String value, Param parent, Position position) {
        super(name, value, parent, position);
    }

    public List<String> getCustomTypes() {
        return getChildren().stream()
                .map(Param::getName)
                .collect(Collectors.toList());
    }
}
