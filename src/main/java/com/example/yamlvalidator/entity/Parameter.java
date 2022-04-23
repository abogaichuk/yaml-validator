package com.example.yamlvalidator.entity;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Optional;

@SuperBuilder
@Getter
@ToString
public abstract class Parameter {
    private String name, path;
//    private boolean editable, unique, bypass;
    private Position position;

    public int getRow() {
        return Optional.ofNullable(position)
                .map(Position::getRow)
                .orElse(-1);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Parameter) {
            Parameter p = (Parameter) obj;
            return name.equals(p.getName());
        }
        return false;
    }
}
