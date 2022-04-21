package com.example.yamlvalidator.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Builder
@Getter
@ToString
public class Definition {
    private String resourceType;
    private String description;
    private List<Parameter> parameters;
    private List<Link> links;
}
