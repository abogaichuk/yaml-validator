package com.example.yamlvalidator.entity;

import com.example.yamlvalidator.rules.PadmGrammar;
import com.example.yamlvalidator.utils.ValidatorUtils;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.yamlvalidator.utils.ValidatorUtils.isNotEmpty;

@Getter
public class ObjectParameter extends Parameter {
    private final List<Parameter> children = new ArrayList<>();

    public ObjectParameter(String name, ParameterType type, ObjectParameter parent, Position position) {
        super(name, type, parent, position);
    }

//    parameter.findChild(PadmGrammar.KeyWord.TYPE.name())
//            .filter(StringParameter.class::isInstance)
//                .map(StringParameter.class::cast)
//                .map(type -> PadmGrammar.StandardType.valueOf(type.getValue().toUpperCase()))
//            .map(standardType -> standardType.validate(parameter))
//            .orElseGet(ValidationResult::valid)

    @Override
    public ValidationResult validate() {
//        return objectRules().validate(this);
        return findChild(PadmGrammar.KeyWord.TYPE.name())
                .filter(StringParameter.class::isInstance)
                .map(StringParameter.class::cast)
                .flatMap(type -> PadmGrammar.StandardType.get(type.getValue()))
                .orElse(PadmGrammar.StandardType.OBJECT)
                .validate(this);
    }

//    @Override
//    public ValidationResult validate(Parameter resource) {
//        return null;
//    }

    public boolean isRequired() {
        return findChild(PadmGrammar.KeyWord.REQUIRED.name())
                .filter(StringParameter.class::isInstance)
                .map(StringParameter.class::cast)
                .flatMap(ValidatorUtils::toBoolean)
                .orElse(false);
    }

    public Optional<Parameter> findChild(String name) {
        return isNotEmpty(name) ? children.stream()
            .filter(param -> name.equalsIgnoreCase(param.getName()))
            .findAny() : Optional.empty();
    }

    public Set<Parameter> getDuplicates() {
        return children.stream()
            .filter(parameter -> Collections.frequency(children, parameter) > 1)
            .collect(Collectors.toSet());
    }

    public boolean containsDuplicates() {
        return !getDuplicates().isEmpty();
    }

    public void addChildren(List<Parameter> parameters) {
        children.addAll(parameters);
    }

//    @Override
//    public boolean equals(Object obj) {
//        return super.equals(obj);
//    }
}
