package com.example.yamlvalidator;

import org.snakeyaml.engine.v2.api.StreamDataWriter;

import java.io.StringWriter;

public class MyStreamToStringWriter extends StringWriter implements StreamDataWriter {
}
