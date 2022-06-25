package com.example.yamlvalidator.mappers;

import com.example.yamlvalidator.utils.MappingUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.example.yamlvalidator.mappers.TestUtils.TestType.*;
import static com.example.yamlvalidator.mappers.TestUtils.data;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SchemaMapperTest {
    private SchemaMapper mapper;

    @BeforeEach
    void setUpd() {
        mapper = new SchemaMapper();
    }

    @Test
    public void when_standardTypeDefinedInType_returnExpected() {
        var pair = data.get(CUSTOM_STANDARD_TYPE);
        var actual = testFlow(pair.getKey());
        assertEquals(pair.getValue(), actual);
    }

    @Test
    public void when_standardTypeDefinedInCustom_returnExpected() {
        var pair = data.get(CUSTOM_STANDARD_TYPE2);
        var actual = testFlow(pair.getKey());
        assertEquals(pair.getValue(), actual);
    }

    @Test
    public void when_customType_returnItsFields() {
        var pair = data.get(CUSTOM_TYPE);
        var actual = testFlow(pair.getKey());
        assertEquals(pair.getValue(), actual);
    }

    @Test
    public void when_customType_returnUpdatedFields() {
        var pair = data.get(CUSTOM_TYPE3_MERGING);
        var actual = testFlow(pair.getKey());
        assertEquals(pair.getValue(), actual);
    }

    @Test
    public void when_complicatedCustomType_returnExpected() {
        var pair = data.get(CUSTOM_TYPE4_RECURSIVE);
        var actual = testFlow(pair.getKey());
        assertEquals(pair.getValue(), actual);
    }

    @Test
    public void when_standardTypes_returnExpected() {
        var pair = data.get(CUSTOM_PRIMITIVES);
        var actual = testFlow(pair.getKey());
        assertEquals(pair.getValue(), actual);
    }

    @Test
    public void when_variantType_returnOneOf() {
        var pair = data.get(VARIANT_TYPE);
        var actual = testFlow(pair.getKey());
        assertEquals(pair.getValue(), actual);
    }

    @Test
    public void when_variantTypeWithCustomParams_returnOneOf() {
        var pair = data.get(VARIANT_TYPE2);
        var actual = testFlow(pair.getKey());
        assertEquals(pair.getValue(), actual);
    }

    @Test
    public void when_sequenceType_returnExpected() {
        var pair = data.get(SEQUENCE_TYPE);
        var actual = testFlow(pair.getKey());
        assertEquals(pair.getValue(), actual);
    }

    @Test
    public void when_sequenceTypeAndCombinedTypes_returnExpected() {
        var pair = data.get(SEQUENCE_TYPE2);
        var actual = testFlow(pair.getKey());
        assertEquals(pair.getValue(), actual);
    }

    private String testFlow(String yaml) {
        return mapper.mapToParam(yaml)
                .map(mapper::mapToNode)
                .map(MappingUtils::nodeToString)
                .orElseThrow(() -> new RuntimeException("testFlow exception!!"));
    }
}
