package com.example.yamlvalidator.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.yamlvalidator.rules.ParameterRuleFactory.enumRules;
import static com.example.yamlvalidator.rules.ParameterRuleFactory.objectRules;
import static com.example.yamlvalidator.utils.ValidatorUtils.isNotEmpty;

@SuperBuilder
@Getter
@Setter
public class ObjectParameter extends Parameter {
    private List<Parameter> children;

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

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
