package com.example.yamlvalidator.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Resource implements Parameter {
    private final String name;
    private final String value;
    private final Resource parent;
    private final List<Parameter> children = new ArrayList<>();
    private final Position position;
    private final YamlType yamlType;

    public Resource(String name, String value, Resource parent, Position position, YamlType yamlType) {
        this.name = name;
        this.value = value;
        this.parent = parent;
        this.position = position;
        this.yamlType = yamlType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public Parameter getParent() {
        return parent;
    }

    @Override
    public Stream<Parameter> getChildren() {
        return children.stream();
    }

    @Override
    public YamlType getType() {
        return yamlType;
    }

    public void addChild(Resource parameter) {
        children.add(parameter);
    }

    public void addChildren(List<Resource> parameters) {
        children.addAll(parameters);
    }

    @Override
    public String toString() {
        return "Resource: " + getName() + ", path: " + getPath() + ", (row #" + getRow() + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Resource) {
            var p = (Resource) obj;
            return getPath().equals(p.getPath());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getPath().hashCode();
    }
}
