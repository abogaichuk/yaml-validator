package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.utils.ValidatorUtils;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class CustomTypesResolver {

    public List<Node> resolve(String type, MappingNode root) {
//        Stream.of(type.split(ValidatorUtils.OR_TYPE_SPLITTER))
//                .map(String::trim)
//                .map()
        return Collections.emptyList();
    }
}
