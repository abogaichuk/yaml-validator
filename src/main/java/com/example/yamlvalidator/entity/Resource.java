package com.example.yamlvalidator.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class Resource implements Parameter {
    private final String name;
    private final String value;
    private final Parameter parent;
    private final List<Parameter> children = new ArrayList<>();
    private final Position position;
    private final YamlType yamlType;

    public Resource(ResourceBuilder builder) {
        this.name = builder.name;
        this.value = builder.value;
        this.parent = builder.parent;
        this.position = builder.position;
        this.yamlType = builder.yamlType;
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
    public Optional<Position> getPosition() {
        return Optional.ofNullable(position);
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

    @Override
    public void addChildren(List<Parameter> parameters) {
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

    public static class ResourceBuilder implements Builder {
        private String name;
        private String value;
        private Parameter parent;
        private Position position;
        private YamlType yamlType;

        public static ResourceBuilder builder() {
            return new ResourceBuilder();
        }

        private ResourceBuilder() {}

        @Override
        public Parameter build()
        {
            return new Resource(this);
        }

        @Override
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        @Override
        public Builder value(String value) {
            this.value = value;
            return this;
        }

        @Override
        public Builder parent(Parameter parent) {
            this.parent = parent;
            return this;
        }

        @Override
        public Builder position(Position position) {
            this.position = position;
            return this;
        }

        @Override
        public Builder yamlType(YamlType type) {
            this.yamlType = type;
            return this;
        }
    }
}
