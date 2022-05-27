package com.example.yamlvalidator.entity;

import lombok.Getter;
import lombok.ToString;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.yamlvalidator.utils.ValidatorUtils.isNotEmpty;

@Getter
@ToString(exclude = {"parent"})
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

    public void addChildren(List<? extends Param> list) {
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
