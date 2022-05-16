package com.example.yamlvalidator.entity;

import com.example.yamlvalidator.rules.PadmGrammar;
import lombok.Getter;
import lombok.ToString;

import java.util.Optional;
import java.util.stream.Stream;

import static com.example.yamlvalidator.utils.ValidatorUtils.isNotEmpty;

@Getter
@ToString
public abstract class Parameter {
    private String name;
    private ParameterType type;
    private Parameter parent;
    private Position position;

    public enum ParameterType {
        SCALAR, SEQUENCE, MAPPING
    }

    public Parameter(String name, ParameterType type, Parameter parent, Position position) {
        this.name = name;
        this.type = type;
        this.parent = parent;
        this.position = position;
    }

    public abstract ValidationResult validate();

    public int getRow() {
        return Optional.ofNullable(position)
                .map(Position::getRow)
                .orElse(-1);
    }

    public String getPath() {
        return parent != null ? parent.getPath() + "/" + name : name;
    }

    public Definition getRoot() {
        Parameter parent = getParent();
        while (parent.getParent() != null) {
            parent = parent.getParent();
        }
        return (Definition) parent;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Parameter) {
            Parameter p = (Parameter) obj;
            return getPath().equals(p.getPath());
//            return name.equals(p.getName());
        }
        return false;
    }

    public boolean isNotASequenceType() {
        return !ParameterType.SEQUENCE.equals(type);
    }

    //if name == type or name != keyword, so it's a new type definition (Test: Manual or Auto)
    //if parent type == sequence, paramname == index in collection
    public boolean isTypeOrNotAKeyword() {
        return isType() || (isNotAKeyword() && parent.isNotASequenceType());
    }

    protected boolean isNotAType(final String type) {
        return isNotAStandardType(type) && isNotACustomType(type);
    }

    private boolean isType() {
        return PadmGrammar.KeyWord.TYPE.name().equalsIgnoreCase(name);
    }

    private boolean isNotAKeyword() {
        return getKeyWord().isEmpty();
    }

    public Optional<PadmGrammar.KeyWord> getKeyWord() {
        return Stream.of(PadmGrammar.KeyWord.values())
                .filter(keyWord -> keyWord.name().equalsIgnoreCase(name))
                .findAny();
    }

    private boolean isNotACustomType(final String type) {
        return !isCustomType(type);
    }

    private boolean isCustomType(final String type) {
        return getRoot().getCustomTypes().stream()
                .anyMatch(t -> t.equalsIgnoreCase(type));
    }

    private boolean isNotAStandardType(String type) {
        return !isStandardType(type);
    }

    private boolean isStandardType(String type) {
        return Stream.of(PadmGrammar.StandardType.values())
                .anyMatch(t -> t.name().equalsIgnoreCase(type));
    }
}
