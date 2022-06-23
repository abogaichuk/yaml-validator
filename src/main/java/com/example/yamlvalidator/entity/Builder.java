package com.example.yamlvalidator.entity;

public interface Builder {
    Builder name(String name);
    Builder value(String value);
    Builder parent(Parameter parent);
    Builder position(Position position);
    Builder yamlType(Parameter.YamlType type);
    Parameter build();
}
