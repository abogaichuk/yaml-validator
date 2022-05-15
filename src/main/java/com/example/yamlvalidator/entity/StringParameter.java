package com.example.yamlvalidator.entity;

import com.example.yamlvalidator.rules.PadmGrammar;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.stream.Stream;

import static com.example.yamlvalidator.rules.PadmGrammar.OR_TYPE_SPLITTER;
import static com.example.yamlvalidator.rules.ParameterRuleFactory.stringRules;

@SuperBuilder
@Getter
public class StringParameter extends Parameter {
    private String value;

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
