package com.example.yamlvalidator.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Getter
@ToString
public class Definition extends ObjectParameter {
    private String resourceType;
    private String description;
//    private List<Parameter> parameters;
    private List<Link> links;

    @Override
    public ValidationResult validate() {
        return super.validate();
//        return parameters
    }
}
