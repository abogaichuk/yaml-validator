package com.example.yamlvalidator.entity;

import com.example.yamlvalidator.grammar.Conditions;
import com.example.yamlvalidator.grammar.KeyWord;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.yamlvalidator.utils.ValidatorUtils.isNotEmpty;

public interface Parameter {
    String getName();
    String getValue();
    Position getPosition();
    Parameter getParent();
    Stream<Parameter> getChildren();
    YamlType getType();

    enum YamlType {
        SCALAR, SEQUENCE, MAPPING
    }

    default String getPath() {
        return getParent() != null && isNotEmpty(getParent().getPath())
                ? getParent().getPath() + "/" + getName()
                : getName();
    }

    default Optional<Parameter> findChild(String paramName) {
        return isNotEmpty(paramName) ? getChildren()
                .filter(param -> paramName.equalsIgnoreCase(param.getName()))
                .findAny() : Optional.empty();
    }

    default int getRow() {
        return Optional.ofNullable(getPosition())
                .map(Position::getRow)
                .orElse(-1);
    }

    default Set<Parameter> getDuplicates() {
        return getChildren()
                .filter(parameter -> Collections.frequency(getChildren().collect(Collectors.toList()), parameter) > 1)
                .collect(Collectors.toSet());
    }

    default Optional<KeyWord> keyWord() {
        return Stream.of(KeyWord.values())
                .filter(keyWord -> keyWord.name().equalsIgnoreCase(getName()))
                .findAny();
    }

    default boolean isNotAKeyword() {
        return keyWord().isEmpty();
    }

    default Stream<Parameter> findCustomFields() {
        return getChildren()
                .filter(Parameter::isNotAKeyword);
    }

    default List<String> findCustomFieldNames() {
        return findCustomFields()
                .map(Parameter::getName)
                .collect(Collectors.toList());
    }

    default boolean isMandatory() {
        return isRoot() && findChild(KeyWord.OPTIONAL.lowerCase())
                .filter(Conditions.boolValueIsTrue)
                .isEmpty();
    }

    private boolean isRoot() {
        return getParent() != null;
    }

    default boolean hasDefaultValue() {
        return findChild(KeyWord.DEFAULT.lowerCase())
                .filter(defaultParam -> isNotEmpty(defaultParam.getValue()))
                .isPresent();
    }

    default String getParentName() {
        return getParent() == null ? "" : getParent().getName();
    }

}
