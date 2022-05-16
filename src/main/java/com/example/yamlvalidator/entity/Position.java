package com.example.yamlvalidator.entity;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public final class Position {
    private final int row;
    private final int column;

    public static Position of(int row, int column) {
        return new Position(row + 1, column);
    }

    private Position(int row, int column) {
        this.row = row;
        this.column = column;

    }
}
