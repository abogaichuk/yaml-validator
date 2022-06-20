package com.example.yamlvalidator.mappers;

import com.example.yamlvalidator.services.SchemaMapper;
import com.example.yamlvalidator.utils.MappingUtils;
import com.example.yamlvalidator.utils.ValidatorUtils;
import org.junit.jupiter.api.Test;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.composer.Composer;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.parser.ParserImpl;
import org.snakeyaml.engine.v2.scanner.StreamReader;

import java.util.Optional;

import static com.example.yamlvalidator.mappers.TestUtils.*;
import static com.example.yamlvalidator.mappers.TestUtils.TestType.*;
import static org.junit.jupiter.api.Assertions.*;

public class SchemaMapperTest {

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
        return toNode(yaml)
                .map(MappingNode.class::cast)
                .map(node -> new SchemaMapper(node).map())
                .map(MappingUtils::map)
                .map(ValidatorUtils::nodeToString)
                .orElseThrow(() -> new RuntimeException("testFlow exception!!"));
    }

    private Optional<Node> toNode(String yaml) {
        var settings = LoadSettings.builder().build();
        var composer = new Composer(settings, new ParserImpl(settings, new StreamReader(settings, yaml)));

        return composer.getSingleNode();
    }
}
