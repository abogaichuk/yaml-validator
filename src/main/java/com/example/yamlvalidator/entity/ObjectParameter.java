package com.example.yamlvalidator.entity;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Getter
public class ObjectParameter extends Parameter {
    private List<? extends Parameter> children;
}
