package com.example.yamlvalidator.entity;

import com.example.yamlvalidator.factory.Rule;
import com.example.yamlvalidator.factory.RulesFactory;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.yamlvalidator.utils.ValidatorUtils.VALIDATOR;
import static com.example.yamlvalidator.utils.ValidatorUtils.isNotEmpty;

@SuperBuilder
@Getter
public class ObjectParameter extends Parameter {
    private List<Parameter> children;

//    @Override
//    public ValidationResult accept(Visitor v) {
//        ValidationResult result = v.visit(this);
//        List<ValidationResult> invalidResults = children.stream()
//                .map(parameter -> parameter.accept(v))
//                .filter(validationResult -> !validationResult.isValid())
//                .collect(Collectors.toList());
//        return invalidResults.stream()
//                .reduce(ValidationResult::merge)
//                .map(childResult -> childResult.merge(result))
//                .orElse(result);
//    }

    @Override
    public ValidationResult validate() {
        Rule rules = RulesFactory.getRules(this);
        ValidationResult result = rules.validate(this);
//        ValidationResult result = Validator.of().validate(this);
        ValidationResult finalResult = children.stream()
                .map(Parameter::validate)
                .reduce(ValidationResult::merge)
                .map(childResult -> childResult.merge(result))
                .orElse(result);
        return finalResult;
    }

    public Optional<Parameter> findChild(String name) {
        return isNotEmpty(name) ? children.stream()
            .filter(param -> name.equalsIgnoreCase(param.getName()))
            .findAny() : Optional.empty();
    }

    public Optional<Parameter> findChildRecursive(String path) {
        if (isNotEmpty(path)) {
            String[] parts = path.split("/", 2);
            if (parts.length > 1) {
                Optional<Parameter> child = findChild(parts[0]);
                if (child.isPresent() && child.get() instanceof ObjectParameter) {
                    return ((ObjectParameter) child.get()).findChildRecursive(parts[1]);
                }
            } else {
                return findChild(path);
            }
        }
        return Optional.empty();
    }

    public Optional<StringParameter> getChildAsString(final String childPath) {
        return findChildRecursive(childPath)
            .filter(p -> p instanceof StringParameter)
            .map(StringParameter.class::cast);
    }

    public Optional<List<StringParameter>> getDescendantAsList(final String childPath) {
        return findChildRecursive(childPath)
                .map(parameter -> {
                    if (parameter instanceof ObjectParameter) {
                        return ((ObjectParameter) parameter).getChildren().stream()
                                .filter(p -> p instanceof StringParameter)
                                .map(p -> (StringParameter) p)
                                .collect(Collectors.toList());
                    } else {
                        return Collections.singletonList((StringParameter) parameter);
                    }
                });

    }

    public Optional<Parameter> findValidatorParam(String name) {
        return isNotEmpty(name) ? findChild(VALIDATOR)
            .filter(p -> p instanceof ObjectParameter)
            .map(ObjectParameter.class::cast)
            .flatMap(p -> p.findChild(name)) : Optional.empty();
    }

    public Set<Parameter> getDuplicates() {
        return children.stream()
            .filter(parameter -> Collections.frequency(children, parameter) > 1)
            .collect(Collectors.toSet());
    }

    public boolean containsDuplicates() {
        return getDuplicates().size() > 0;
    }

    public String getTypeChildValue() {
        return findChildRecursive("Type")
            .filter(parameter -> parameter instanceof StringParameter)
            .map(StringParameter.class::cast)
            .map(StringParameter::getValue)
            .orElse("custom");
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
