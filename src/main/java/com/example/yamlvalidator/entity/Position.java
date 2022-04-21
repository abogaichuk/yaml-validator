package com.example.yamlvalidator.entity;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Position {
    private final int row;
    private final int column;

    public static Position of(int row, int column) {
        return new Position(row, column);
    }

    private Position(int row, int column) {
        this.row = row;
        this.column = column;

    }
}
