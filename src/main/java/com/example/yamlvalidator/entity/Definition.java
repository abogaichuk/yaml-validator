package com.example.yamlvalidator.entity;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.stream.Collectors;

//@SuperBuilder
@Getter
@ToString
public class Definition extends ObjectParameter {
    private String resourceType;
    private String description;
    private List<Link> links;

    public Definition(String name, ParameterType type, Parameter parent, Position position, List<Parameter> children, String resourceType, List<Link> links) {
        super(name, type, parent, position, children);
        this.resourceType = resourceType;
        this.links = links;
    }


    @Override
    public ValidationResult validate() {
        return super.validate();
    }

    public List<String> getCustomTypes() {
        return getChildren().stream()
                .map(Parameter::getName)
                .collect(Collectors.toList());
    }
}
