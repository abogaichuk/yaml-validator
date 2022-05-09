package com.example.yamlvalidator.entity;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class StringParameter extends Parameter {
    private String value;

    //todo stringparam validation for keywords value or custom types??
    //todo stream over all parameters with ruleFactory or abstractfctory
    @Override
    public ValidationResult validate() {
        System.out.println("Validate parameter: " + getName() + ", value: " + value);
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
