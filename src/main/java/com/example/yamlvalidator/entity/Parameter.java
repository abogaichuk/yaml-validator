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
    Parameter getParent();
    Stream<Parameter> getChildren();
    YamlType getType();
    void addChildren(List<Parameter> parameters);

    enum YamlType {
        SCALAR, SEQUENCE, MAPPING
    }

    default String getPath() {
        return getParent() != null && isNotEmpty(getParent().getPath())
                ? getParent().getPath() + "/" + getName()
                : getName();
    }

    default Optional<Parameter> deepSearch(String path) {
        if (isNotEmpty(path)) {
            var parts = path.split("/", 2);
            return parts.length > 1
                ? findChild(parts[0]).flatMap(child -> child.deepSearch(parts[1]))
                : findChild(path);
        }
        return Optional.empty();
    }

    default Optional<Parameter> findChild(String paramName) {
        return isNotEmpty(paramName) ? getChildren()
                .filter(param -> paramName.equalsIgnoreCase(param.getName()))
                .findAny() : Optional.empty();
    }

    default Optional<Position> getPosition() {
        return Optional.empty();
    }

    default int getRow() {
        return getPosition()
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

    default boolean isOptional() {
        return isRoot() || findChild(KeyWord.OPTIONAL.lowerCase())
                .filter(Conditions.boolValueIsTrue)
                .isPresent();
    }

    default boolean isMandatory() {
        return !isOptional();
    }

    private boolean isRoot() {
        return getParent() == null;
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
