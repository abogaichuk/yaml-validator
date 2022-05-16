package com.example.yamlvalidator.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.yamlvalidator.rules.ParameterRuleFactory.objectRules;
import static com.example.yamlvalidator.utils.ValidatorUtils.isNotEmpty;

@Getter
public class ObjectParameter extends Parameter {
    private final List<Parameter> children = new ArrayList<>();

    public ObjectParameter(String name, ParameterType type, Parameter parent, Position position) {
        super(name, type, parent, position);
    }

    @Override
    public ValidationResult validate() {
        return objectRules().validate(this);
//        return ParameterType.MAPPING.equals(getType()) ? objectRules().validate(this) : enumRules().validate(this);
    }

    public Optional<Parameter> findChild(String name) {
        return isNotEmpty(name) ? children.stream()
            .filter(param -> name.equalsIgnoreCase(param.getName()))
            .findAny() : Optional.empty();
    }

    public Set<Parameter> getDuplicates() {
        return children.stream()
            .filter(parameter -> Collections.frequency(children, parameter) > 1)
            .collect(Collectors.toSet());
    }

    public boolean containsDuplicates() {
        return getDuplicates().size() > 0;
    }

    public boolean addChildren(List<Parameter> parameters) {
        return children.addAll(parameters);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
