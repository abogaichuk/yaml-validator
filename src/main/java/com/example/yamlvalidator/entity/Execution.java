package com.example.yamlvalidator.entity;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class Execution {
    private final String definition;
    private final String resource;
    private final boolean resolvePlaceholders;
    private final boolean includeDefaults;
    private final boolean includeSecrets;
    private final boolean preview;
}
