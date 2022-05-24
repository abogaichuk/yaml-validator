package com.example.yamlvalidator.entity;

import com.example.yamlvalidator.rules.Validatable;
import lombok.Getter;
import lombok.ToString;
import org.jooq.lambda.tuple.Tuple;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
@ToString
public class Definition extends ObjectParameter {
    private final String resourceType;
//    private final String description;
    private final List<Link> links;

    public Definition(String name, ParameterType type, ObjectParameter parent, Position position,
                      String resourceType, List<Link> links) {
        super(name, type, parent, position);
        this.resourceType = resourceType;
        this.links = links;
    }

    public List<String> getCustomTypes() {
        return getChildren().stream()
                .map(Parameter::getName)
                .collect(Collectors.toList());
    }

//    @Override
//    public ValidationResult validate(List<Parameter> resources) {
//        ValidationResult result = this.validate();
//
//        return result;
//    }

//    private Map<Parameter, Parameter> schemaAgainstResources()
}
