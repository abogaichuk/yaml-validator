package com.example.yamlvalidator.entity;

import com.example.yamlvalidator.grammar.KeyWord;
import com.example.yamlvalidator.grammar.SchemaRule;
import com.example.yamlvalidator.grammar.StandardType;

import java.util.Optional;
import java.util.stream.Stream;

import static com.example.yamlvalidator.entity.ValidationResult.invalid;
import static com.example.yamlvalidator.utils.ValidatorUtils.*;

public class SchemaParam extends Param {
    public SchemaParam(String name, String value, Param parent, Position position) {
        super(name, value, parent, position);
    }

    public final ValidationResult validate() {
        ValidationResult result = validateSelf();

        //temporary fix, skip validation for keyword children
        return isNotAKeyword() ? getChildren().stream()
                .map(SchemaParam.class::cast)
                .map(SchemaParam::validate)
                .reduce(result, ValidationResult::merge) : result;
    }

    // get appropriate rules
    // if param has a child type - validate through the rules
    // in other case, if param doesn't have a child, check the param through correctTypeRule
    private ValidationResult validateSelf() {
        return findChild(KeyWord.TYPE.name())
                .map(Param::getValue)
                .map(typeName -> StandardType.getOrDefault(typeName).getRule())
                .orElseGet(this::correctType) //only for scalar params
                .validate(this);
    }

    private SchemaRule correctType() {
        return param -> {
            //todo move to Mapper? Scheme type validation it's the difference between schema and resource
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

    private Optional<String> getTypeValue() {
        //todo sequence indexes
        return isCustomTypeDeclaration() ? Optional.of(getValue()) : Optional.empty();
    }

    //if name == type or name != keyword, so it's a new type definition (Test: Manual or Auto)
    //if parent type == sequence, paramname == index in collection
    private boolean isCustomTypeDeclaration() {
        return isNotEmpty(getName()) && isNotEmpty(getValue())
                && (KeyWord.TYPE.name().equalsIgnoreCase(getName()) || isNotAKeyword());
    }

    private boolean isNotAType(final String splittedType) {
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
                .filter(keyWord -> keyWord.name().equalsIgnoreCase(getName()))
                .findAny();
    }
}
