package com.example.yamlvalidator.entity;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.example.yamlvalidator.ValidatorUtils.VALIDATOR;
import static com.example.yamlvalidator.ValidatorUtils.isNotEmpty;

@SuperBuilder
@Getter
public class ObjectParameter extends Parameter {
    private List<? extends Parameter> children;

    public Optional<? extends Parameter> findChild(String name) {
        return isNotEmpty(name) ? children.stream()
            .filter(param -> name.equals(param.getName()))
            .findAny() : Optional.empty();
    }

    public Optional<? extends Parameter> findChildRecursive(String path) {
        if (isNotEmpty(path)) {
            String[] parts = path.split("/", 2);
            if (parts.length > 1) {
                Optional<? extends Parameter> child = findChild(parts[0]);
                if (child.isPresent() && child.get() instanceof ObjectParameter) {
                    return ((ObjectParameter) child.get()).findChild(parts[1]);
                }
            } else {
                return findChild(path);
            }
        }
        return Optional.empty();
    }

    public Optional<StringParameter> getChildAsString(final String childPath) {
        return findChildRecursive(childPath)
            .filter(p -> p instanceof StringParameter)
            .map(StringParameter.class::cast);
    }

    public Optional<? extends Parameter> findValidatorParam(String name) {
        return isNotEmpty(name) ? findChild(VALIDATOR)
            .filter(p -> p instanceof ObjectParameter)
            .map(ObjectParameter.class::cast)
            .flatMap(p -> p.findChild(name)) : Optional.empty();
    }

    public Set<? extends Parameter> getDuplicates() {
        return children.stream()
            .filter(parameter -> Collections.frequency(children, parameter) > 1)
            .collect(Collectors.toSet());
    }

    public String getTypeFieldValue() {
        return findChildRecursive("Type")
            .filter(parameter -> parameter instanceof StringParameter)
            .map(StringParameter.class::cast)
            .map(StringParameter::getValue)
            .orElse("unknown");
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
