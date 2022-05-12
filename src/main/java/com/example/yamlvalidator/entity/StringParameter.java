package com.example.yamlvalidator.entity;

import com.example.yamlvalidator.utils.PadmGrammar;
import com.example.yamlvalidator.utils.ValidatorUtils;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.NodeTuple;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.yamlvalidator.entity.ValidationResult.*;
import static com.example.yamlvalidator.utils.PadmGrammar.*;
import static com.example.yamlvalidator.utils.ValidatorUtils.*;
import static com.example.yamlvalidator.validators.Conditions.compareNums;

@SuperBuilder
@Getter
public class StringParameter extends Parameter {
    private String value;

    //todo stream over all parameters with ruleFactory or abstractfctory
    @Override
    public ValidationResult validate() {
//        if (isTypeOrCustomParam()) {
//            return typeValidation();
//        } else if (isDefaultParameter()) {
//            return defaultAgainstType()
//                    .merge(defaultAgainstValidator());
//        } else {
//            return valid();
//        }
        return valid();
    }

    private boolean isDefaultParameter() {
        return DEFAULT_KEY_NAME.equalsIgnoreCase(getName());
    }

    private ValidationResult defaultAgainstValidator() {
        return getFellowByName(VALIDATORS_KEY_NAME)
                .filter(parameter -> parameter instanceof ObjectParameter)
                .map(ObjectParameter.class::cast)
                .flatMap(validators -> validators.getChildren().stream()
                        .map(validator -> {
                            if (MIN_KEY_NAME.equalsIgnoreCase(validator.getName()) && compareInts((StringParameter) validator, this)) {
                                return invalid(toErrorMessage(this, DEFAULT_LESS_THAN_MIN));
                            } else if (MAX_KEY_NAME.equalsIgnoreCase(validator.getName()) && !compareInts((StringParameter) validator, this)) {
                                return invalid(toErrorMessage(this, DEFAULT_MORE_THAN_MAX));
                            } else if (LIST_KEY_NAME.equalsIgnoreCase(validator.getName())) {
                                List<String> acceptedValues = ((ObjectParameter) validator).getChildren().stream()
                                        .filter(sp -> sp instanceof StringParameter)
                                        .map(StringParameter.class::cast)
                                        .map(StringParameter::getValue)
                                        .collect(Collectors.toList());
                                return acceptedValues.contains(value) ? valid() : invalid(toErrorMessage(this, DEFAULT_WRONG));
                            } else if (AFTER_KEY_NAME.equalsIgnoreCase(validator.getName()) && compareDates((StringParameter) validator, this)) {
                                return invalid(toErrorMessage(this, DEFAULT_IS_BEFORE_AFTER));
                            } else if (BEFORE_KEY_NAME.equalsIgnoreCase(validator.getName()) && !compareDates((StringParameter) validator, this)) {
                                return invalid(toErrorMessage(this, DEFAULT_IS_AFTER_BEFORE));
                            } else {
                                return valid();
                            }
                        })
                        .reduce(ValidationResult::merge))
                .orElseGet(ValidationResult::valid);
    }

    private ValidationResult defaultAgainstType() {
        return getFellowByName(TYPE_KEY_NAME)
                .filter(parameter -> parameter instanceof StringParameter)
                .map(StringParameter.class::cast)
                .flatMap(sp -> {
                    return Stream.of(sp.getValue().split(OR_KEYWORD))
                            .map(String::trim)
                            .map(type -> {
                                if (NUMBER_TYPE.equalsIgnoreCase(type) && toInt(this).isEmpty()) {
                                    return invalid(toErrorMessage(this, value, DEFAULT_IS_NOT_NUMBER));
                                } else if (DATETIME_TYPE.equalsIgnoreCase(type) && toDatetime(this).isEmpty()) {
                                    return invalid(toErrorMessage(this, value, DEFAULT_IS_NOT_DATETIME));
                                } else if (BOOLEAN_TYPE.equalsIgnoreCase(type) && toBoolean(this).isEmpty()) {
                                    return invalid(toErrorMessage(this, value, DEFAULT_IS_NOT_BOOL));
                                } else {
                                    return valid();
                                }
                            }).reduce(ValidationResult::merge);
                }).orElseGet(ValidationResult::valid);
    }

//    private ValidationResult typeValidation() {
//        return Stream.of(value.split(OR_KEYWORD))
//                .map(String::trim)
//                .filter(this::isWrong)
//                .map(type -> invalid(toErrorMessage(this, type, UNKNOWN_TYPE)))
//                .reduce(ValidationResult::merge)
//                .orElseGet(ValidationResult::valid);
//    }

    private Optional<Parameter> getFellowByName(String name) {
        return ((ObjectParameter) getParent()).findChild(name);
    }

    private boolean isWrong(String type) {
        return isNotAStandardType(type) && isNotACustomType(type);
    }

    public boolean isTypeOrCustomParam() {
        return isTypeParam() || isNotAKeyword() && isNotValidator(); //type param or custom param like Test: Manual or Auto
    }

    private boolean isNotValidator() {
        return !LIST_KEY_NAME.equalsIgnoreCase(getParent().getName());
    }

    private boolean isTypeParam() {
        return TYPE_KEY_NAME.equalsIgnoreCase(getName());
    }

    private boolean isNotAKeyword() {
        return !isKeyword();
    }

    private boolean isKeyword() {
        return keywords.stream().anyMatch(word -> word.equalsIgnoreCase(getName()));
    }

    private boolean isNotACustomType(String type) {
        return !isCustomType(type);
    }

    private boolean isCustomType(String type) {
        return getRoot().getCustomTypes().stream()
                .anyMatch(t -> t.equalsIgnoreCase(type));
    }

    private boolean isNotAStandardType(String type) {
        return !isStandardType(type);
    }

    private boolean isStandardType(String type) {
        return standardTypes.stream()
                .anyMatch(t -> t.equalsIgnoreCase(type));
    }

    @Override
    public boolean equals(Object obj) {
//        if (obj instanceof StringParameter) {
//            return  ((StringParameter) obj).getValue().equals(value);
//        } else {
//            return false;
//        }
        return super.equals(obj);
    }
}
