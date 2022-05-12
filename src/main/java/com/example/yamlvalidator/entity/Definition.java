package com.example.yamlvalidator.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@SuperBuilder
@Getter
@ToString
public class Definition extends ObjectParameter {
    private String resourceType;
    private String description;
//    private List<Parameter> parameters;
//    private List<Parameter> types;
    private List<Link> links;

    @Override
    public ValidationResult validate() {
        return super.validate();
//        return parameters
    }

    public List<String> getCustomTypes() {
        return getChildren().stream()
                .map(Parameter::getName)
                .collect(Collectors.toList());
    }

    public List<Parameter> getAllTypes() {
        return getChildren();
    }
}
