package com.example.yamlvalidator.entity;

import lombok.Getter;

import java.util.stream.Stream;

import static com.example.yamlvalidator.rules.PadmGrammar.OR_TYPE_SPLITTER;
import static com.example.yamlvalidator.rules.ParameterRuleFactory.stringRules;

@Getter
public class StringParameter extends Parameter {
    private final String value;

    public StringParameter(String name, ParameterType type, Parameter parent, Position position, String value) {
        super(name, type, parent, position);
        this.value = value;
    }

    @Override
    public ValidationResult validate() {
        return stringRules().validate(this);
    }

    public boolean isWrongType() {
        return Stream.of(value.split(OR_TYPE_SPLITTER))
                .map(String::trim)
                .anyMatch(this::isNotAType);
    }

    @Override
    public boolean equals(Object obj) {
//        if (obj instanceof StringParameter) {
//            return  ((StringParameter) obj).getValue().equals(value);
//        } else {
//            return false;
//        }
        return super.equals(obj);
    }
}
