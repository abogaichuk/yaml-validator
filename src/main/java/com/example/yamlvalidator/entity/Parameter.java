package com.example.yamlvalidator.entity;

import com.example.yamlvalidator.grammar.KeyWord;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.yamlvalidator.utils.ValidatorUtils.isNotEmpty;

@Getter
public class Parameter {
    private final String name;
    private final Object value;
    private final String path;
//    private Parameter parent; //we don't need parent because of all custom types are resolved by mapper
    private final Position position;

    private Parameter(String name, String path, Object value, Position position) {
        this.name = name;
        this.path = path;
        this.value = value;
        this.position = position;
    }

    public static Parameter of(String name, String path, Object value, Position position) {
        return new Parameter(name, path, value, position);
    }

    public Parameter update(List<Parameter> customTypes) {
        if ("SwitchSecondSecond".equalsIgnoreCase(getName())) {
            List<Parameter> children = List.of(new Parameter("boolean", "", "boolean", position),
                    new Parameter("string", "", "string", position));
            Parameter oneOF = new Parameter("oneOf", getPath() + "oneOf", children, position);
            return oneOF;
        } else if (value instanceof String){
            return this;
        } else {
            List<Parameter> list = ((List<Parameter>) value).stream()
                    .map(child -> child.update(customTypes)).collect(Collectors.toList());
            return new Parameter(getName(), getPath(), list, getPosition());
        }
    }

//    public Parameter(Parameter other) {
//        this.name = other.getName();
//        this.value = other.getValue();
//        this.path = other.getPath();
//        this.position = other.getPosition();
//    }

    //if name == type or name != keyword, so it's a new type definition (Test: Manual or Auto)
    //if parent type == sequence, paramname == index in collection
//    public boolean isCustomTypeDefinition() {
//        Optional<String> typeValue =
//        return isNotEmpty(getName())
//                && (value instanceof String || hasChildType
//                && (KeyWord.TYPE.name().equalsIgnoreCase(getName())
//                || isNotAKeyword()); //skip keywords except type
//    }

    public Optional<String> findTypeValue() {
        if (value instanceof String && isNotAKeyword()) {
            return Optional.of((String) value);
        } else {
            return findChild(KeyWord.TYPE.name())
                    .map(String.class::cast); //todo throw an error?
        }
    }

    public Optional<Parameter> findChild(String paramName) {
        return isNotEmpty(paramName) && value instanceof List
                ? findChild(paramName, (List<Parameter>) value)
                : Optional.empty();
    }

    private Optional<Parameter> findChild(String parameterName, List<Parameter> children) {
        return children.stream()
                .filter(parameter -> parameterName.equalsIgnoreCase(parameter.getName()))
                .findAny();
    }

//    public Parameter hasCustomType() {
//        Optional<String> typeValue = findTypeValue();
//
//    }

    public boolean isNotAKeyword() {
        return getKeyWord().isEmpty();
    }

    public Optional<KeyWord> getKeyWord() {
        return Stream.of(KeyWord.values())
                .filter(keyWord -> keyWord.name().equalsIgnoreCase(getName()))
                .findAny();
    }
}
