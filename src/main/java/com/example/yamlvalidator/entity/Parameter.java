package com.example.yamlvalidator.entity;

import com.example.yamlvalidator.rules.PadmGrammar;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.example.yamlvalidator.rules.Conditions.isObjectParameter;
import static com.example.yamlvalidator.rules.Conditions.isStringParameter;
import static com.example.yamlvalidator.rules.PadmGrammar.OR_TYPE_SPLITTER;
import static com.example.yamlvalidator.utils.ValidatorUtils.*;

//@SuperBuilder
@Getter
@ToString
public abstract class Parameter {
    private String name;
    private ParameterType type;
    private Optional<PadmGrammar.KeyWord> keyWord;
    private Parameter parent;
//    private boolean editable, unique, bypass;
    private Position position;

    public enum ParameterType {
        SCALAR, SEQUENCE, MAPPING
    }

    public Parameter(String name, ParameterType type, Parameter parent, Position position) {
        this.name = name;
        this.keyWord = Stream.of(PadmGrammar.KeyWord.values())
                .filter(kw -> kw.name().equalsIgnoreCase(name))
                .findAny();
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
        return !type.equals(ParameterType.SEQUENCE);
    }

    //if name == type or name != keyword, so it's a new type definition (Test: Manual or Auto)
    //todo if name is empty == collection type
    public boolean isTypeOrNotAKeyword() {
        return isNotEmpty(name) && (isType() || isNotAKeyword());
    }

    protected boolean isNotAType(final String type) {
        return isNotAStandardType(type) && isNotACustomType(type);
    }

    private boolean isType() {
        return PadmGrammar.KeyWord.TYPE.name().equalsIgnoreCase(name);
    }

    private boolean isNotAKeyword() {
        return !isKeyWord();
    }

    private boolean isKeyWord() {
        return Stream.of(PadmGrammar.KeyWord.values()).anyMatch(keyWord -> keyWord.name().equalsIgnoreCase(name));
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

    public String getKeyWordError() {
        return keyWord.map(kw -> kw.paramType.message).orElse("");
    }

//    public enum KeyWord {
//        TYPE(KeyWordType.STRING),
//        ITEMS(KeyWordType.OBJECT),
//        ENUM(KeyWordType.OBJECT),
//        ONEOF(KeyWordType.OBJECT),
//        ANYOF(KeyWordType.OBJECT),
//        PROPERTIES(KeyWordType.OBJECT),
//        PATTERN(KeyWordType.STRING),
//        DESCRIPTION(KeyWordType.STRING),
//        DEFAULT(KeyWordType.STRING),
//        REQUIRED(KeyWordType.STRING),
//        EXAMPLE(KeyWordType.STRING),
//        BYPASS(KeyWordType.STRING),
//        MIN(KeyWordType.STRING),
//        MAX(KeyWordType.STRING),
//        LIST(KeyWordType.OBJECT),
//        AFTER(KeyWordType.STRING),
//        BEFORE(KeyWordType.STRING);
//
//        public final KeyWordType paramType;
//        KeyWord(KeyWordType paramType) {
//            this.paramType = paramType;
//        }
//    }
//
//    public enum KeyWordType {
//        STRING(STRING_KEYWORD, isStringParameter),
//        OBJECT(OBJECT_KEYWORD, isObjectParameter);
//
//        public final String message;
//        public final Predicate<Parameter> predicate;
//        KeyWordType(String message, Predicate<Parameter> predicate) {
//            this.message = message;
//            this.predicate = predicate;
//        }
//    }
}
