package com.example.yamlvalidator.grammar;

public enum KeyWord {
    TYPE,
    TYPES,
    ITEMS,
    ENUM,
    UNIQUE,
    ONEOF,
    ANYOF,
    OPTIONAL,
    PROPERTIES,
    PATTERN,
    DESCRIPTION,
    DEFAULT,
    REQUIRED,
    EXAMPLE,
    BYPASS,
    MIN,
    MAX,
    LIST,
    AFTER,
    BEFORE;

    public String lowerCase() {
        return this.name().toLowerCase();
    }
}
