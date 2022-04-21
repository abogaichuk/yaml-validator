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
    private boolean editable, unique, bypass;
    private Position position;

    public int getColumn() {
        return Optional.ofNullable(position)
                .map(Position::getColumn)
                .orElse(-1);
    }
}
