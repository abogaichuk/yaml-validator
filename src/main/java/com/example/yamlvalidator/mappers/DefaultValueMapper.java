package com.example.yamlvalidator.mappers;

import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.Resource;
import com.example.yamlvalidator.entity.Schema;
import com.example.yamlvalidator.errors.PadmGrammarException;
import com.example.yamlvalidator.grammar.KeyWord;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.ScalarNode;

import static com.example.yamlvalidator.utils.MappingUtils.getName;
import static com.example.yamlvalidator.utils.MappingUtils.getPosition;
import static com.example.yamlvalidator.utils.ValidatorUtils.EMPTY;
import static org.snakeyaml.engine.v2.nodes.NodeType.*;

public class DefaultValueMapper extends PlaceholderMapper {
    private Schema schema;

    public DefaultValueMapper(Schema schema) {
        super(Resource.ResourceBuilder.builder());
        this.schema = schema;
    }

    protected Parameter toParameter(final Node keyNode, final Node valueNode, final Parameter parent) {
        if (KeyWord.DEFAULT.lowerCase().equals(getName(keyNode))) {
            //todo no, resource could be null, we should run via schema params
            return null;
        } else {
            return super.toParameter(keyNode, valueNode, parent);
        }
    }
}
