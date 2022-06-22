package com.example.yamlvalidator.services;


import com.example.yamlvalidator.utils.MappingUtils;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.composer.Composer;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.parser.ParserImpl;
import org.snakeyaml.engine.v2.scanner.StreamReader;

import java.util.Optional;
import java.util.regex.Pattern;

public class PlaceHolderResolver {
    private static final Pattern pattern = Pattern.compile(".*?\\$\\{(\\w+)\\}.*?");
    private final MappingNode node;

    public PlaceHolderResolver(MappingNode node) {
        this.node = node;
    }

    public Optional<Node> resolve(String src) {
        var yaml = "# value: 8080 #need new keyword for resource? default or value or?\n" +
                "Database:\n" +
                "  Credentials:\n" +
                "    Username: admin\n" +
                "    Password: nimda";
//        var yaml = "ccc";
        return MappingUtils.stringToNode(yaml);
    }

    public boolean match(String src) {
        var matcher = pattern.matcher(src);
        return matcher.matches();
    }
}
