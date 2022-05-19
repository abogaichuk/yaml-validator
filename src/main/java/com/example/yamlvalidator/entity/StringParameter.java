package com.example.yamlvalidator.entity;

import com.example.yamlvalidator.rules.ParameterRule;
import lombok.Getter;

import java.util.stream.Stream;

import static com.example.yamlvalidator.rules.PadmGrammar.*;

@Getter
public class StringParameter extends Parameter {
    private final String value;

    public StringParameter(String name, ParameterType type, Parameter parent, Position position, String value) {
        super(name, type, parent, position);
        this.value = value;
    }

    @Override
    public ValidationResult validate() {
        return correctType()
                .and((ParameterRule<StringParameter>) keyWordRule())
                .validate(this);
    }

    public boolean isWrongType() {
        return Stream.of(value.split(OR_TYPE_SPLITTER))
                .map(String::trim)
                .anyMatch(this::isNotAType);
    }
}
