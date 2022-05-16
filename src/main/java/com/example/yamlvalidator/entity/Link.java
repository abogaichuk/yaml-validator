package com.example.yamlvalidator.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Link {
    private final String resourceType;
    private final String relation;
    private final boolean required;
}
