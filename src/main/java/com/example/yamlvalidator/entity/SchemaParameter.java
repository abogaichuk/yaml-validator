//package com.example.yamlvalidator.entity;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Stream;
//
//public class SchemaParameter implements Parameter {
//    private final String name;
//    private final String value;
//    private final SchemaParameter parent;
//    private final List<Parameter> children = new ArrayList<>();
//    private final Position position;
//    private final YamlType yamlType;
//
//    public SchemaParameter(String name, String value, SchemaParameter parent, Position position, YamlType yamlType) {
//        this.name = name;
//        this.value = value;
//        this.parent = parent;
//        this.position = position;
//        this.yamlType = yamlType;
//    }
//
//    //    public ValidationResult validate(RuleService rules, Resource resource) {
////        ValidationResult self = getType().ruleFunction.apply(rules).validate(this, resource);
////        return getChildren().stream()
////                .map(SchemaParam.class::cast)
////                .map(param -> param.validate(rules, getAppropriateResource(param.getName(), resource)))
////                .reduce(self, ValidationResult::merge);
////    }
////
////    protected Resource getAppropriateResource(String name, Param resource) {
////        return resource == null ? null : resource.getChildren().stream()
////                .filter(child -> name.equalsIgnoreCase(child.getName()))
////                .map(Resource.class::cast)
////                .findAny().orElse(null);
////    }
////
////    public StandardType getType() {
////        return StandardType.getOrDefault(getTypeValue());
////    }
//
//    @Override
//    public String getName() {
//        return name;
//    }
//
//    @Override
//    public String getValue() {
//        return value;
//    }
//
//    @Override
//    public Position getPosition() {
//        return position;
//    }
//
//    @Override
//    public Parameter getParent() {
//        return parent;
//    }
//
//    @Override
//    public Stream<Parameter> getChildren() {
//        return children.stream();
//    }
//
//    @Override
//    public YamlType getType() {
//        return yamlType;
//    }
//
//    public void addChild(SchemaParameter parameter) {
//        children.add(parameter);
//    }
//
//    public void addChildren(List<SchemaParameter> parameters) {
//        children.addAll(parameters);
//    }
//}
