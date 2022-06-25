package com.example.yamlvalidator.entity;

import com.example.yamlvalidator.grammar.KeyWord;
import com.example.yamlvalidator.grammar.RuleService;
import com.example.yamlvalidator.grammar.StandardType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.example.yamlvalidator.entity.ValidationResult.valid;

public class Schema implements Parameter {
    private final String name;
    private final String value;
    private final Parameter parent;
    private final List<Parameter> children = new ArrayList<>();
    private final Position position;
    private final YamlType yamlType;

    public Schema(SchemaBuilder builder) {
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

    public void deleteIfPresent(String name) {
        children.stream()
                .filter(child -> child.getName().equalsIgnoreCase(name))
                .findAny()
                .ifPresent(children::remove);
    }

    public ValidationResult validate(RuleService rules) {
        return validate(rules, null);
    }

    public ValidationResult validate(RuleService rules, Resource resource) {
        var result = validateSelf(rules, resource);

        return result.isValid()
                ? getChildren()
                    .filter(Parameter::isNotAKeyword)
                    .map(Schema.class::cast)
                    .map(param -> param.validate(rules, getAppropriateResource(param.getName(), resource)))
                    .reduce(valid(), ValidationResult::merge)
                : result;
    }

    private ValidationResult validateSelf(RuleService rules, Resource resource) {
        return StandardType.getOrDefault(getTypeValue()).ruleFunction.apply(rules).validate(this, resource);
    }

    public String getTypeValue() {
        return findChild(KeyWord.TYPE.name())
                .map(Parameter::getValue)
                .orElse(this.getValue());
    }

    public Resource getAppropriateResource(String name, Resource resource) {
        return resource == null ? null : resource.getChildren()
                .filter(child -> name.equalsIgnoreCase(child.getName()))
                .map(Resource.class::cast)
                .findAny().orElse(null);
    }

    @Override
    public String toString() {
        return "Schema: " + getName() + ", path: " + getPath() + ", (row #" + getRow() + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Schema) {
            var p = (Schema) obj;
            return getPath().equals(p.getPath());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getPath().hashCode();
    }

    public static class SchemaBuilder implements Builder {
        private String name;
        private String value;
        private Parameter parent;
        private Position position;
        private YamlType yamlType;

        public static SchemaBuilder builder() {
            return new SchemaBuilder();
        }

        private SchemaBuilder() {}

        @Override
        public Parameter build()
        {
            return new Schema(this);
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
