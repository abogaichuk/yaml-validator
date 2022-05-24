package com.example.yamlvalidator.entity;

import lombok.Data;

import java.util.List;

@Data
public class ValidationRequest {
    private Definition schema;
    private List<Parameter> resources;
}
