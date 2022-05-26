package com.example.yamlvalidator.entity;

import com.example.yamlvalidator.grammar.KeyWord;
import com.example.yamlvalidator.grammar.StandardType;
import com.example.yamlvalidator.grammar.SchemaRule;
import com.example.yamlvalidator.utils.ValidatorUtils;
import lombok.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.yamlvalidator.entity.ValidationResult.invalid;
import static com.example.yamlvalidator.entity.ValidationResult.valid;
import static com.example.yamlvalidator.utils.ValidatorUtils.*;
import static com.example.yamlvalidator.utils.ValidatorUtils.HAS_DUPLICATES;

@Getter
@ToString(exclude={"parent"})
public class Param {
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
        return parent != null ? parent.getPath() + "/" + name : name;
    }

    public Set<Param> getDuplicates() {
        return children.stream()
                .filter(parameter -> Collections.frequency(children, parameter) > 1)
                .collect(Collectors.toSet());
    }

    public Optional<Param> findChild(String name) {
        return isNotEmpty(name) ? children.stream()
                .filter(param -> name.equalsIgnoreCase(param.getName()))
                .findAny() : Optional.empty();
    }

    public final ValidationResult validate() {
        ValidationResult result = validateSelf();

        //temporary fix, skip validation for keyword children
        return isNotAKeyword() ? getChildren().stream()
                .map(Param::validate)
                .reduce(result, ValidationResult::merge) : result;
    }

    // get appropriate rules
    // if param has a child type - validate through the rules
    // in other case, if param doesn't have a child check the param through correctTypeRule
    private ValidationResult validateSelf() {
        return findChild(KeyWord.TYPE.name())
                .map(Param::getValue)
                .map(typeName -> StandardType.getOrDefault(typeName).getRule())
                .orElseGet(this::correctType) //only for scalar params
                .validate(this);
    }

    private SchemaRule correctType() {
        return param -> {   //todo move to Mapper? Scheme type validation it's the difference between schema and resource
                            //todo + for move, because of placeholders which can modify the types too
            Optional<String> incorrectValue = param.getTypeValue()
                    .flatMap(typeValue -> Stream.of(typeValue.split(OR_TYPE_SPLITTER))
                            .map(String::trim)
                            .filter(this::isNotAType)
                            .findAny());
            return incorrectValue.map(s -> invalid(toErrorMessage(param, s, UNKNOWN_TYPE)))
                    .orElseGet(ValidationResult::valid);
        };
    }

    //if name == type or name != keyword, so it's a new type definition (Test: Manual or Auto)
    //if parent type == sequence, paramname == index in collection
    private Optional<String> getTypeValue() {
        if (isNotEmpty(name) && isNotEmpty(value) && (KeyWord.TYPE.name().equalsIgnoreCase(name) || isNotAKeyword())) {
            return Optional.of(value); //todo sequence indexes
        } else {
            return Optional.empty();
//            return findChild(PadmGrammar.KeyWord.TYPE.name())
//                    .map(Param::getValue);
        }
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

    private boolean isNotAKeyword() {
        return getKeyWord().isEmpty();
    }

    public Optional<KeyWord> getKeyWord() {
        return Stream.of(KeyWord.values())
                .filter(keyWord -> keyWord.name().equalsIgnoreCase(name))
                .findAny();
    }

    public Schema getRoot() {
        Param p = getParent();
        while (p.getParent() != null) {
            p = p.getParent();
        }
        return (Schema) p;
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
