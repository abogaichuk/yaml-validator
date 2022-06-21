package com.example.yamlvalidator.entity;

import com.example.yamlvalidator.grammar.Conditions;
import com.example.yamlvalidator.grammar.KeyWord;
import com.example.yamlvalidator.grammar.RuleService;
import com.example.yamlvalidator.grammar.StandardType;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.yamlvalidator.entity.ValidationResult.*;
import static com.example.yamlvalidator.utils.ValidatorUtils.isNotEmpty;

@Builder
public class Schema implements Parameter {
    private final String name;
    private final String value;
    private final Schema parent;
    private final List<Parameter> children = new ArrayList<>();
    private final Position position;
    private final YamlType yamlType;

    public Schema(String name, String value, Schema parent, Position position, YamlType yamlType) {
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

    public void addChild(Schema parameter) {
        children.add(parameter);
    }

    public void addChildren(List<Schema> parameters) {
        children.addAll(parameters);
    }

    public void deleteIfPresent(String name) {
        children.stream()
                .filter(child -> child.getName().equalsIgnoreCase(name))
                .findAny()
                .ifPresent(children::remove);
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
}
