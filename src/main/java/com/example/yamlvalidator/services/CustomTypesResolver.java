package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.Schema;
import com.example.yamlvalidator.utils.ValidatorUtils;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeTuple;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.example.yamlvalidator.utils.ValidatorUtils.*;
import static com.example.yamlvalidator.utils.ValidatorUtils.isNotAStandardType;

public class CustomTypesResolver {
    private final NodeTuple types;

    public CustomTypesResolver(NodeTuple types) {
        this.types = types;
    }

    public List<Node> resolve(String name, Schema parent, String value) {
//        Stream.of(type.split(ValidatorUtils.OR_TYPE_SPLITTER))
//                .map(String::trim)
//                .map()
        return Collections.emptyList();
    }

    //if paramname == type or paramname is not a keyword(custom type) and value is not a standard type
    public boolean match(String paramName, String typeValue) {
        return (isTypeKeyWord(paramName) || (isNotEmpty(paramName) && isNotAKeyword(paramName))) && isNotAStandardType(typeValue);
    }
}
