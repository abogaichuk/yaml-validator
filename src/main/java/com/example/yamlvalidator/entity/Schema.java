package com.example.yamlvalidator.entity;

import org.apache.logging.log4j.util.Strings;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.yamlvalidator.utils.ValidatorUtils.isNotEmpty;

public class Schema extends SchemaParam {
    public Schema(String name, String value, Param parent, Position position) {
        super(name, value, parent, position);
    }

    public List<String> getCustomTypes() {
        return getChildren().stream()
                .map(Param::getName)
                .collect(Collectors.toList());
    }

    @Override
    public String getPath() {
        return Strings.EMPTY;
    }

//    public ValidationResult validate(List<Resource> resources) {
//        getChildren().stream()
//                .map(Schema.class::cast)
//                .map(schemaParam -> findResource(schemaParam, resources))
//
//    }
//
//    private ValidationResult validate(SchemaParam param, List<Resource> resources) {
//        findResource(param, resources)
//                .map()
//    }

//    private ValidationResult validate(SchemaParam param, Optional<Resource> resourceOptional) {
//
//    }

    private Optional<Resource> findResource(SchemaParam param, List<Resource> resources) {
        return resources.stream()
                .filter(resource -> deepSearch(param.getPath()).isPresent())
                .findAny();
    }

    //todo must validate all schema params because of resource can be missed for particular schema param
//    public ValidationResult validate(Resource resource) {
//        Optional<Param> schemaParam = deepSearch(resource.getPath());
//    }

//    public Optional<SchemaParam> findSchemaParamForResource(Resource resource) {
//        Optional<Param> schemaParam = deepSearch(resource.getPath());
//        return Optional.empty();
//    }
}
