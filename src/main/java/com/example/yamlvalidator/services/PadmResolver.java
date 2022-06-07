package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.Definition;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.grammar.StandardType;
import com.example.yamlvalidator.utils.ValidatorUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.example.yamlvalidator.utils.ValidatorUtils.OR_TYPE_SPLITTER;

public class PadmResolver {
    private final PlaceHolderResolver placeHolderService;
    private final CustomTypesResolver customTypesResolver;

    public PadmResolver(PlaceHolderResolver placeHolderService, CustomTypesResolver customTypesResolver) {
        this.placeHolderService = placeHolderService;
        this.customTypesResolver = customTypesResolver;
    }

    public Definition resolve(Definition definition) {
//        definition.getParameters().stream()
//                .filter(this::needUpdate)
//                .map()
        return null;
    }

    private boolean needUpdate(Parameter parameter) {
        return parameter.findTypeValue()
                .map(typeValue -> isNotAStandardType(typeValue) || typeValue.split(OR_TYPE_SPLITTER).length > 1)
                .orElse(Boolean.FALSE);
    }

    private Parameter update(Parameter old, List<Parameter> customTypes) {
        Optional<Parameter> created = old.findTypeValue()
                .map(typeValue -> typeValue.split(OR_TYPE_SPLITTER))
                .map(types -> {
                    if (types.length > 1) {
                        return createOneOf(types, customTypes);
                    } else {
                        return createCustom(types[0], customTypes);
                    }
                });
        return created.get();
    }

    private Parameter createOneOf(String[] types, List<Parameter> customTypes) {
        return null;
    }

    private Parameter createCustom(String type, List<Parameter> customTypes) {
        return null;
    }

    private boolean isNotAStandardType(final String type) {
        return !isStandardType(type);
    }

    private boolean isStandardType(String splittedType) {
        return Stream.of(StandardType.values())
                .anyMatch(t -> t.name().equalsIgnoreCase(splittedType));
    }
}
