package com.example.yamlvalidator.rules;

import com.example.yamlvalidator.entity.Resource;
import com.example.yamlvalidator.entity.Schema;
import com.example.yamlvalidator.entity.ValidationResult;
import com.example.yamlvalidator.grammar.RuleService;
import com.example.yamlvalidator.mappers.PlaceholderMapper;
import com.example.yamlvalidator.mappers.SchemaMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.example.yamlvalidator.rules.TestData.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class RuleServiceTests {

    private RuleService rules;
    private SchemaMapper schemaMapper;
    private PlaceholderMapper resourceMapper;

    @BeforeEach
    void setup() {
        rules = new RuleService();
        schemaMapper = new SchemaMapper();
        resourceMapper = new PlaceholderMapper(Resource.ResourceBuilder.builder());
    }

    @Test
    public void when_incorrectNumber_returnError() {
        ValidationResult result = schemaTestFlow(WRONG_NUMBER);
        assertTrue(!result.isValid() && result.getReasons().size() == 3);
    }

    @Test
    public void when_incorrectDatetime_returnError() {
        ValidationResult result = schemaTestFlow(WRONG_DATETIME);
        assertTrue(!result.isValid() && result.getReasons().size() == 3);
    }

    @Test
    public void when_incorrectBoolean_returnError() {
        assertFalse(schemaTestFlow(WRONG_BOOLEAN).isValid());
    }

    @Test
    public void when_incorrectStandardType_returnError() {
        assertFalse(schemaTestFlow(WRONG_STANDARD_TYPE).isValid());
    }

    @Test
    public void when_incorrectNumbersConfig_returnError() {
        assertFalse(schemaTestFlow(MAX_LESS_THAN_MIN).isValid());
        assertFalse(schemaTestFlow(DEFAULT_LESS_THAN_MIN).isValid());
        assertFalse(schemaTestFlow(DEFAULT_MORE_THAN_MAX).isValid());
        assertFalse(schemaTestFlow(LIST_DOESNT_CONTAIN_DEFAULT).isValid());
    }

    @Test
    public void when_incorrectDatetimeConfig_returnError() {
        assertFalse(schemaTestFlow(DEFAULT_AFTER_BEFORE).isValid());
        assertFalse(schemaTestFlow(DEFAULT_BEFORE_AFTER).isValid());
        assertFalse(schemaTestFlow(AFTER_IS_AFTER_BEFORE).isValid());
    }

    @Test
    public void when_hasDuplicates_returnError() {
        assertFalse(schemaTestFlow(DUPLICATES).isValid());
    }

    @Test
    public void when_isBypass_returnValid() {
        ValidationResult result = schemaTestFlow(BYPASS);
        assertTrue(result.getReasons().size() == 1 && result.getReasons().stream()
                .anyMatch(message -> message.contains("validation is skipped")));
    }

    @Test
    public void when_allParamsAreOptional_returnValid() {
        var pair = validationData.get(ValidationType.OPTIONAL);
        assertTrue(validationTestFlow(pair.getKey(), pair.getValue()).isValid());
    }

    @Test
    public void when_paramIsMandatoryButMissed_returnError() {
        var pair = validationData.get(ValidationType.MANDATORY);
        assertFalse(validationTestFlow(pair.getKey(), pair.getValue()).isValid());
    }

    @Test
    public void when_paramIsMandatoryButAtLeastOneChildMissed_returnError() {
        var pair = validationData.get(ValidationType.MANDATORY2);
        assertFalse(validationTestFlow(pair.getKey(), pair.getValue()).isValid());
    }

    @Test
    public void when_resourceDoesntMatchOneOfStandardTypes_returnError() {
        var pair = validationData.get(ValidationType.ONE_OF);
        assertFalse(validationTestFlow(pair.getKey(), pair.getValue()).isValid());
    }

    @Test
    public void when_resourceDoesntMatchOneOfCustomTypes_returnError() {
        var pair = validationData.get(ValidationType.ONE_OF2);
        assertFalse(validationTestFlow(pair.getKey(), pair.getValue()).isValid());
    }

    private ValidationResult validationTestFlow(String schemaYaml, String resourceYaml) {
        return schemaMapper.mapToParam(schemaYaml)
                .map(Schema.class::cast)
                .map(schema -> schema.validate(rules,
                        resourceMapper.mapToParam(resourceYaml)
                                .map(Resource.class::cast)
                                .orElse(null)))
                .orElseGet(ValidationResult::valid);
    }

    private ValidationResult schemaTestFlow(String yaml) {
        return schemaMapper.mapToParam(yaml)
                .map(Schema.class::cast)
                .map(schema -> schema.validate(rules))
                .orElseGet(ValidationResult::valid);
    }
}
