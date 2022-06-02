package com.example.yamlvalidator.entity;

import org.apache.logging.log4j.util.Strings;

public class Schema extends SchemaParam {
    public Schema(String name, String value, Param parent, Position position) {
        super(name, value, parent, position);
    }

    @Override
    public String getPath() {
        return Strings.EMPTY;
    }
}
