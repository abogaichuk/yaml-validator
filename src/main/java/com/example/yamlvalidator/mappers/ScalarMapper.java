package com.example.yamlvalidator.mappers;

import com.example.yamlvalidator.entity.Builder;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.Position;

public class ScalarMapper extends AbstractMapper {

    public ScalarMapper() {
        super();
    }

    public ScalarMapper(Builder builder) {
        super(builder);
    }

    @Override
    protected Parameter scalarParsing(String name, String value, Parameter parent, Position position) {
        return build(name, value, parent, position, Parameter.YamlType.SCALAR);
    }
}
