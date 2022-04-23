package com.example.yamlvalidator.entity;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class StringParameter extends Parameter {
    private String value;

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
