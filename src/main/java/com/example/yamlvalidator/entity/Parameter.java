package com.example.yamlvalidator.entity;

import com.example.yamlvalidator.rules.PadmGrammar;
import lombok.Getter;
import lombok.ToString;

import java.util.Optional;
import java.util.stream.Stream;

@Getter
@ToString
public abstract class Parameter {
    private final String name;
    private final ParameterType type;
    private final ObjectParameter parent;
    private final Position position;

    public enum ParameterType {
        SCALAR, SEQUENCE, MAPPING
    }

    protected Parameter(String name, ParameterType type, ObjectParameter parent, Position position) {
        this.name = name;
        this.type = type;
        this.parent = parent;
        this.position = position;
    }

    public abstract ValidationResult validate();
//    public abstract ValidationResult validate(Parameter resource);

    public int getRow() {
        return Optional.ofNullable(position)
                .map(Position::getRow)
                .orElse(-1);
    }

    public String getPath() {
        return parent != null ? parent.getPath() + "/" + name : name;
    }

    public Definition getRoot() {
        ObjectParameter p = getParent();
        while (p.getParent() != null) {
            p = p.getParent();
        }
        return (Definition) p;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Parameter) {
            Parameter p = (Parameter) obj;
            return getPath().equals(p.getPath());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getPath().hashCode();
    }

    public boolean isNotASequenceType() {
        return !ParameterType.SEQUENCE.equals(type);
    }

    //if name == type or name != keyword, so it's a new type definition (Test: Manual or Auto)
    //if parent type == sequence, paramname == index in collection
    public boolean isTypeOrNotAKeyword() {
        return isType() || (isNotAKeyword() && parent.isNotASequenceType());
    }

    protected boolean isNotAType(final String splittedType) {
        return isNotAStandardType(splittedType) && isNotACustomType(splittedType);
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
        return Stream.of(PadmGrammar.StandardType.values())
                .anyMatch(t -> t.name().equalsIgnoreCase(splittedType));
    }
}
