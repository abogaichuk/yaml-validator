package com.example.yamlvalidator.mappers;

import com.example.yamlvalidator.entity.Builder;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.Position;
import com.example.yamlvalidator.errors.PadmGrammarException;
import com.example.yamlvalidator.utils.MappingUtils;
import lombok.Getter;
import org.snakeyaml.engine.v2.nodes.*;

import java.util.Optional;
import java.util.regex.Pattern;

import static com.example.yamlvalidator.grammar.KeyWord.TYPES;
import static com.example.yamlvalidator.utils.MappingUtils.getName;
import static com.example.yamlvalidator.utils.ValidatorUtils.EMPTY;

public class PlaceholderMapper extends ScalarMapper {
    private static final Pattern pattern = Pattern.compile(".*?\\$\\{(\\w+)\\}.*?");
    @Getter
    private Optional<NodeTuple> nodeTypes;

    public PlaceholderMapper(Builder builder) {
        super(builder);
    }

    @Override
    public Optional<Parameter> mapToParam(String yaml) {
        this.nodeTypes = MappingUtils.stringToNode(yaml)
                .filter(root -> NodeType.MAPPING.equals(root.getNodeType()))
                .map(MappingNode.class::cast)
                .flatMap(mappingNode -> mappingNode.getValue().stream()
                        .filter(tuple -> TYPES.lowerCase().equals(getName(tuple.getKeyNode())))
                        .findAny());

        return super.mapToParam(yaml);
    }

    @Override
    public Parameter scalarParsing(String name, String value, Parameter parent, Position position) {
        if (match(value)) {
            Optional<Parameter> parameter = MappingUtils.stringToNode(resolve(value))
                    .map(node -> {
                        if (node instanceof ScalarNode) {
                            return build(name, ((ScalarNode) node).getValue(), parent, position, Parameter.YamlType.SCALAR);
                        } else if (node instanceof CollectionNode) {
                            return build(name, EMPTY, parent, position, Parameter.YamlType.MAPPING, node);
                        } else {
                            throw new PadmGrammarException("map param with name!");
                        }
                    });;
            if (parameter.isPresent())
                return parameter.get();
        }
        return super.scalarParsing(name, value, parent, position);
    }

    private String resolve(String src) {
//        var yaml = "# value: 8080 #need new keyword for resource? default or value or?\n" +
//                "Database:\n" +
//                "  Credentials:\n" +
//                "    Username: admin\n" +
//                "    Password: nimda";
        var yaml = "ccc";
//        return MappingUtils.stringToNode(yaml);
        return yaml;
    }

    private boolean match(String src) {
        var matcher = pattern.matcher(src);
        return matcher.matches();
    }
}
