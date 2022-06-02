package com.example.yamlvalidator.entity;

import com.example.yamlvalidator.grammar.*;
import com.example.yamlvalidator.utils.ValidatorUtils;
import org.apache.logging.log4j.util.Strings;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.yamlvalidator.utils.ValidatorUtils.*;
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

//    public ValidationResult validate(RuleService rules, Resource resource) {
//        var r = getAppropriateResource(getName(), resource.getChildren());
//        return getType().ruleFunction.apply(rules).validate(this, r);
//    }

    @Override
    public String getPath() {
        return Strings.EMPTY;
    }

    public Stream<ValidationResult> validateResources(List<Resource> resources) {
        return getChildren().stream()
                .map(schemaParam -> {
                    return mandatoryParamValidator()
                            .and(incorrectTypeValidator())
                            .or(lessThenMinValidator().and(moreThenMaxValidator()))
                            .validate(schemaParam, getResource(schemaParam.getName(), resources));
                });
    }

    private ValidationRule incorrectTypeValidator() {
        return (schemaParam, resource) -> {
//            String typeValue = schemaParam.findChild(KeyWord.TYPE.name())
//                    .map(Param::getValue)
//                    .orElse(schemaParam.getValue());
//            Stream.of(typeValue.split(OR_TYPE_SPLITTER))
//                    .map(String::trim)

            return ValidationResult.valid();
        };
    }

//    private boolean isTypeAcceptable(String splittedType, Resource resource) {
//        //todo the same in schemaparam and StandardType.getOrDefault
//        Optional<StandardType> standardType = Stream.of(StandardType.values())
//                .filter(t -> t.name().equalsIgnoreCase(splittedType))
//                .findFirst();
//        standardType
//                .map(t -> t.validate(resource))
//                .filter(ValidationResult::isValid)
//                .orElse(getCustomTypes().)
//    }

    private ValidationRule lessThenMinValidator() {
        return (schemaParam, resource) -> schemaParam.findChild(KeyWord.MIN.name())
                .flatMap(ValidatorUtils::toInt)
                .filter(min -> resource == null || toInt(resource)
                        .map(value -> min >= value)
                        .orElse(Boolean.TRUE))
                .map(min -> ValidationResult.invalid(toErrorMessage(schemaParam, MESSAGE_LESS_THAN)))
                .orElseGet(ValidationResult::valid);
    }

    private ValidationRule moreThenMaxValidator() {
        return (schemaParam, resource) -> schemaParam.findChild(KeyWord.MAX.name())
                .flatMap(ValidatorUtils::toInt)
                .filter(max -> resource == null || toInt(resource)
                        .map(value -> max <= value)
                        .orElse(Boolean.TRUE))
                .map(min -> ValidationResult.invalid(toErrorMessage(schemaParam, MESSAGE_MORE_THAN)))
                .orElseGet(ValidationResult::valid);
    }

    private ValidationRule mandatoryParamValidator() {
        return (schemaParam, resource) -> {
            if (isMandatory(schemaParam) && !hasDefaultValue(schemaParam)) {
                if (resource == null || isEmpty(resource.getValue()))
                    return ValidationResult.invalid(toErrorMessage(schemaParam, MANDATORY_PARAMETER));
            }
            return ValidationResult.valid();
        };
    }

    private Resource getResource(String name, List<Resource> resources) {
        return resources.stream()
                .filter(resource -> name.equalsIgnoreCase(resource.getName()))
                .findAny().orElse(null);
    }

    private boolean hasDefaultValue(Param schemaParam) {
        return schemaParam.findChild(KeyWord.DEFAULT.name())
                .filter(defaultParam -> isNotEmpty(defaultParam.getValue()))
                .isPresent();
    }

    private boolean isMandatory(Param schemaParam) {
        return schemaParam.findChild(KeyWord.REQUIRED.name())
                .filter(Conditions.boolValueIsTrue)
                .isPresent();
    }
}
