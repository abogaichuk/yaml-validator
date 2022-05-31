package com.example.yamlvalidator.grammar;

import com.example.yamlvalidator.entity.Param;
import com.example.yamlvalidator.entity.Resource;
import com.example.yamlvalidator.entity.SchemaParam;
import com.example.yamlvalidator.entity.ValidationResult;
import com.example.yamlvalidator.services.MessageProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.example.yamlvalidator.entity.ValidationResult.invalid;
import static com.example.yamlvalidator.entity.ValidationResult.valid;
import static com.example.yamlvalidator.grammar.Conditions.*;
import static com.example.yamlvalidator.grammar.Conditions.isBoolean;
import static com.example.yamlvalidator.grammar.KeyWord.*;
import static com.example.yamlvalidator.grammar.RulesBuilder.doubleFieldsValidation;
import static com.example.yamlvalidator.grammar.RulesBuilder.singleFieldValidation;
import static com.example.yamlvalidator.utils.ValidatorUtils.*;

@Component
public class RuleService {
    @Autowired
    private MessageProvider messageProvider;

    public ValidationResult validate(SchemaParam param, List<Resource> resources) {
        var resource = getAppropriateResource(param.getName(), resources);
        return param.getType().ruleFunction.apply(this).validate(param, resource);
    }

    private Resource getAppropriateResource(String name, List<Resource> resources) {
        return resources.stream()
                .filter(resource -> name.equalsIgnoreCase(resource.getName()))
                .findAny().orElse(null);
    }

    ValidationRule objects() {
        return bypass().or(noDuplicates());
//        return bypass().or(noDuplicates());
    }

    ValidationRule customs() {
        return (schema, resource) -> ValidationResult.valid();
    }

    private ValidationRule bypass() {
//        return singleFieldValidation(KeyWord.BYPASS.name(), PARAMETER_BYPASS, boolValueIsTrue);
        return (schema, resource) -> schema.findChild(BYPASS.name())
                .filter(boolValueIsTrue)
                .map(p -> invalid(messageProvider.getMessage(PARAMETER_BYPASS, p)))
                .orElseGet(ValidationResult::valid);
    }

    private ValidationRule noDuplicates() {
        return (schema, resource) -> {
            var duplicates = schema.getDuplicates();
            return duplicates.isEmpty() ? valid() : invalid(
                    messageProvider.getMessage(
                            HAS_DUPLICATES,
                            schema.getName(),
                            duplicates.stream()
                                    .map(Param::getName)
                                    .findFirst().get())
            );
        };
    }

    ValidationRule datetime() {
        return (schema, resource) -> ValidationResult.valid();
//        return  objects()
//                .or(incorrectDatetimePatternRules()
//                        .or(comparingDatesRule()));
    }

//    //todo datetime custom pattern or default pattern value?
//    private ValidationRule incorrectDatetimePatternRules() {
//        return singleFieldValidation(AFTER.name(), IS_NOT_A_DATETIME, isDateTime.negate())
//                .and(singleFieldValidation(BEFORE.name(), IS_NOT_A_DATETIME, isDateTime.negate()))
//                .and(singleFieldValidation(DEFAULT.name(), IS_NOT_A_DATETIME, isDateTime.negate()));
////            return doubleFieldsValidation(PATTERN.name(), DEFAULT.name(), DATETIME_PARSED_ERROR, toDateTime);
//    }
//
//    private ValidationRule comparingDatesRule() {
//        return doubleFieldsValidation(AFTER.name(), BEFORE.name(), IS_BEFORE, compareDates)
//                .and(doubleFieldsValidation(BEFORE.name(), DEFAULT.name(), IS_AFTER, compareDates.negate()))
//                .and(doubleFieldsValidation(AFTER.name(), DEFAULT.name(), IS_BEFORE, compareDates));
//    }

    ValidationRule numbers() {
        return objects().or(isNotNan());
//        return objects()
//                .or(isNotNan());
//                        .or(comparingNumbersRule().and(listContainsDefaultRule())));
    }

    private ValidationRule isNotNan() {
//        return (schema, resource) ->  Stream.of(KeyWord.DEFAULT.name(), KeyWord.MIN.name(), KeyWord.MAX.name())
//                .map(schema::findChild)
//                .filter(Optional::isPresent)
//                .map(Optional::get)
//                .filter(Conditions.isNAN)
//                .map(p -> invalid(messageProvider.getMessage(IS_NAN, p.getName(), p.getPath(), p.getRow())))
//                .reduce(ValidationResult.valid(), ValidationResult::merge);
        return (schema, resource) -> schemaParamsAreNAN().validate((SchemaParam) schema)
                .merge(isNAN.test(resource)
                        ? invalid(messageProvider.getMessage(IS_NAN, resource.getName(), resource.getPath(), resource.getRow()))
                        : valid());
    }

//    private ResourceRule isANumber() {
//        return resource -> toInt(resource.getValue())
//    }

    private ValidationRule of(SchemaRule schemaRule) {
        return (schema, resource) -> schemaRule.validate((SchemaParam) schema);
    }

    private SchemaRule schemaParamsAreNAN() {
        return schema -> Stream.of(KeyWord.DEFAULT.name(), KeyWord.MIN.name(), KeyWord.MAX.name())
                .map(schema::findChild)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(Conditions.isNAN)
                .map(p -> invalid(messageProvider.getMessage(IS_NAN, p.getName(), p.getPath(), p.getRow())))
                .reduce(ValidationResult.valid(), ValidationResult::merge);
    }

    private SchemaRule comparingNumbersRule() {
        return doubleFieldsValidation(MIN.name(), MAX.name(), LESS_THAN, compareNums)
                .and(doubleFieldsValidation(MIN.name(), DEFAULT.name(), LESS_THAN, compareNums))
                .and(doubleFieldsValidation(MAX.name(), DEFAULT.name(), MORE_THAN, compareNums.negate()));
    }

    private SchemaRule listContainsDefaultRule() {
        return doubleFieldsValidation(LIST.name(), DEFAULT.name(), DEFAULT_WRONG, listContains.negate());
    }

    ValidationRule booleans() {
        return (schema, resource) -> ValidationResult.valid();
//        return objects()
//                .or(singleFieldValidation(DEFAULT.name(), IS_NOT_A_BOOLEAN, isBoolean.negate()));
    }

    ValidationRule strings() {
        return (schema, resource) -> ValidationResult.valid();
//        return listContainsDefaultRule();
    }
}
