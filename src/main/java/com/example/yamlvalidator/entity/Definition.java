package com.example.yamlvalidator.entity;

import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Definition {
    @Getter
    private final List<Parameter> parameters;

    public Definition(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public Optional<Parameter> getParameter(String name) {
        return parameters.stream()
                .filter(parameter -> parameter.getName().equals(name))
                .findAny();
    }

    public Definition update() {
//        List<Parameter> list = parameters.stream()
//                .map(parameter -> {
//                    if (parameter.getName().equalsIgnoreCase("aaa")) {
//                        return Parameter.of(parameter.getName(), parameter.getPath(), "ccc", parameter.getPosition());
//                    } else {
//                        return parameter;
//                    }
//                }).collect(Collectors.toList());
        List<Parameter> list = parameters.stream().map(parameter -> parameter.update(Collections.emptyList())).collect(Collectors.toList());
        return new Definition(list);
    }
}
