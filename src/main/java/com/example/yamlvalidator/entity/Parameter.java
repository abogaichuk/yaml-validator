package com.example.yamlvalidator.entity;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Optional;

@SuperBuilder
@Getter
@ToString
public abstract class Parameter {
    private String name;
    private ParameterType type;
    private Parameter parent;
//    private boolean editable, unique, bypass;
    private Position position;

    public abstract ValidationResult validate();

    public int getRow() {
        return Optional.ofNullable(position)
                .map(Position::getRow)
                .orElse(-1);
    }

    public String getPath() {
        return parent != null ? parent.getPath() + "/" + name : name;
    }

    public Definition getRoot() {
        Parameter parent = getParent();
        while (parent.getParent() != null) {
            parent = parent.getParent();
        }
        return (Definition) parent;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Parameter) {
            Parameter p = (Parameter) obj;
            return getPath().equals(p.getPath());
//            return name.equals(p.getName());
        }
        return false;
    }

    public boolean isNotASequenceType() {
        return !type.equals(ParameterType.SEQUENCE);
    }

    public enum ParameterType {
        SCALAR, SEQUENCE, MAPPING
    }
}
