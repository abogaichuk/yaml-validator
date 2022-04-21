package com.example.yamlvalidator.entity;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.text.MessageFormat;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidationError {
    private String parameterName;
    //Parameter 'Authentication' is not part of the resource definition (row #2)
    private String message;

    public static ValidationError of(String message) {
        return ValidationError.of(message, null, 0);
    }

    public static ValidationError of(String message, String parameterName, int column) {
        message = MessageFormat.format("{0} paramname: {1} (row #{2})", message, parameterName, column);

        ValidationError error = new ValidationError();
        error.setParameterName(parameterName);
        error.setMessage(message);
        return error;
    }

    @Override
    public String toString() {
        return message;
    }
}
