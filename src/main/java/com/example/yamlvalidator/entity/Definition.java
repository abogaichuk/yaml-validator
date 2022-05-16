package com.example.yamlvalidator.entity;

import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@ToString
public class Definition extends ObjectParameter {
    private final String resourceType;
//    private final String description;
    private final List<Link> links;

    public Definition(String name, ParameterType type, Parameter parent, Position position, String resourceType, List<Link> links) {
        super(name, type, parent, position);
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
