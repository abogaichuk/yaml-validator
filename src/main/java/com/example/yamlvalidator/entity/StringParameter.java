package com.example.yamlvalidator.entity;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class StringParameter extends Parameter {
    private String value;

    @Override
    public ValidationResult validate() {
        return ValidationResult.valid();
    }

    @Override
    public boolean equals(Object obj) {
//        if (obj instanceof StringParameter) {
//            return  ((StringParameter) obj).getValue().equals(value);
//        } else {
//            return false;
//        }
        return super.equals(obj);
    }
}
