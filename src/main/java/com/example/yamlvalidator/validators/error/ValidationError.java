package com.example.yamlvalidator.validators.error;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidationError {
    private int line;
    private int column;
    private String cause;

    public static ValidationError of(int line, int column, String cause) {
        ValidationError error = new ValidationError();
        error.setLine(line);
        error.setColumn(column);
        error.setCause(cause);
        return error;
    }
}
