package com.example.yamlvalidator.entity;

import com.example.yamlvalidator.grammar.KeyWord;
import com.example.yamlvalidator.grammar.StandardType;
import com.example.yamlvalidator.utils.ValidatorUtils;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.yamlvalidator.utils.ValidatorUtils.OR_TYPE_SPLITTER;
import static com.example.yamlvalidator.utils.ValidatorUtils.isNotEmpty;

@Getter
public abstract class Param {
    private final String name;
    private final String value;
    private final Param parent;
    private final List<Param> children = new ArrayList<>();
    private final Position position;

    public Param(String name, String value, Param parent, Position position) {
        this.name = name;
        this.value = value;
        this.parent = parent;
        this.position = position;
    }

    public void addChildren(List<Param> list) {
        children.addAll(list);
    }

    public int getRow() {
        return Optional.ofNullable(position)
                .map(Position::getRow)
                .orElse(-1);
    }

    public String getPath() {
        return parent != null && isNotEmpty(parent.getPath()) ? parent.getPath() + "/" + name : name;
    }

    public Set<Param> getDuplicates() {
        return children.stream()
                .filter(parameter -> Collections.frequency(children, parameter) > 1)
                .collect(Collectors.toSet());
    }

    public Optional<Param> findChild(String paramName) {
        return isNotEmpty(paramName) ? children.stream()
                .filter(param -> paramName.equalsIgnoreCase(param.getName()))
                .findAny() : Optional.empty();
    }

    protected Optional<Param> deepSearch(String path) {
        if (isNotEmpty(path)) {
            String[] parts = path.split("/", 2);
            return parts.length > 1
                    ? findChild(parts[0])
                            .flatMap(child -> child.deepSearch(parts[1]))
                    : findChild(path);
        }
        return Optional.empty();
    }

    public String getTypeValue() {
//        //todo sequence indexes
        return findChild(KeyWord.TYPE.name())
                .map(Param::getValue)
                .orElse(this.getValue());
    }

    public Optional<String> findIncorrectTypeValue() {
        return isCustomTypeDeclaration() ? Stream.of(getTypeValue().split(OR_TYPE_SPLITTER))
                .map(String::trim)
                .filter(ValidatorUtils::isNotEmpty)
                .filter(this::isNotAType)
                .findAny() : Optional.empty();
    }

    //if name == type or name != keyword, so it's a new type definition (Test: Manual or Auto)
    //if parent type == sequence, paramname == index in collection
    private boolean isCustomTypeDeclaration() {
        return isNotEmpty(getName()) && isNotEmpty(getValue())
                && (KeyWord.TYPE.name().equalsIgnoreCase(getName()) || isNotAKeyword());
    }

    protected boolean isNotAType(final String splittedType) {
        return isNotAStandardType(splittedType) && isNotACustomType(splittedType);
    }

    private boolean isNotACustomType(final String splittedType) {
        return !isCustomType(splittedType);
    }

    private boolean isCustomType(final String splittedType) {
        return getRoot().getCustomTypes().stream()
                .anyMatch(t -> t.equalsIgnoreCase(splittedType));
    }

    private boolean isNotAStandardType(final String splittedType) {
        return !isStandardType(splittedType);
    }

    private boolean isStandardType(String splittedType) {
        return Stream.of(StandardType.values())
                .anyMatch(t -> t.name().equalsIgnoreCase(splittedType));
    }

    protected boolean isNotAKeyword() {
        return getKeyWord().isEmpty();
    }

    public Optional<KeyWord> getKeyWord() {
        return Stream.of(KeyWord.values())
                .filter(keyWord -> keyWord.name().equalsIgnoreCase(getName()))
                .findAny();
    }

    private Param getRoot() {
        Param p = this;
        while (p.getParent() != null) {
            p = p.getParent();
        }
        return p;
    }

    private List<String> getCustomTypes() {
        return getChildren().stream()
                .map(Param::getName)
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "Parameter: " + getName() + ", path: " + getPath() + ", (row #" + getRow() + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Param) {
            Param p = (Param) obj;
            return getPath().equals(p.getPath());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getPath().hashCode();
    }
}
