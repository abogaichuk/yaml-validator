package com.example.yamlvalidator.services;


import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.composer.Composer;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.parser.ParserImpl;
import org.snakeyaml.engine.v2.scanner.StreamReader;

import java.io.FileReader;
import java.util.Optional;
import java.util.regex.Pattern;

public class PlaceHolderResolver {
    private static final Pattern pattern = Pattern.compile(".*?\\$\\{(\\w+)\\}.*?");

    public Optional<Node> resolve(String src) {
        var yaml = "# value: 8080 #need new keyword for resource? default or value or?\n" +
                "Database:\n" +
                "  Credentials:\n" +
                "    User: admin\n" +
                "    Password: nimda";
//        var yaml = "ccc";
        var loadSettings = LoadSettings.builder()
                .setParseComments(true)
                .build();
//        Load load = new Load(loadSettings);
//        Object o = load.loadFromString(yaml);
        var reader = new StreamReader(loadSettings, yaml);
        var parser = new ParserImpl(loadSettings, reader);
        var composer = new Composer(loadSettings, parser);

        return composer.getSingleNode();
    }

    public boolean match(String src) {
        var matcher = pattern.matcher(src);
        return matcher.matches();
    }
}
