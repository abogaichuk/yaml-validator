package com.example.yamlvalidator.services;

import com.example.yamlvalidator.MyStreamToStringWriter;
import com.example.yamlvalidator.entity.Definition;
import com.example.yamlvalidator.entity.ValidationError;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.composer.Composer;
import org.snakeyaml.engine.v2.exceptions.ParserException;
import org.snakeyaml.engine.v2.exceptions.ScannerException;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.parser.ParserImpl;
import org.snakeyaml.engine.v2.scanner.StreamReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class YamlService {
    public void execute(String definitionFile, String resourceFile) throws IOException {
        try {
            Optional<Node> defNode = readFile(definitionFile);
//            Optional<Node> resource = readFile(resourceFile);

            YamlMapper mapper = new YamlMapper();
            Optional<Definition> definition = mapper.toDefinition(defNode);
            System.out.println(definition);

            ValidationService validator = new ValidationServiceImpl();
            if (definition.isPresent()) {
                List<ValidationError> errors = validator.validate(definition.get());
                errors.forEach(System.out::println);
            } else {
                System.out.println("parsing exception");
            }

            save(defNode.get(), "definition1.yaml");
//            save(resource.get(), "resource1.yaml");
        } catch (ParserException | ScannerException e) {
            System.out.println(e.getMessage());
        }
    }
    private Optional<Node> readFile(String filename) throws FileNotFoundException {
        var loadSettings = LoadSettings.builder()
                .setParseComments(true)
                .build();

        var reader = new StreamReader(loadSettings, new FileReader(filename));
        var parser = new ParserImpl(loadSettings, reader);
        var composer = new Composer(loadSettings, parser);

        return composer.getSingleNode();
    }

    private void save(Node root, String filename) throws IOException {
        var settings = DumpSettings.builder().build();
        var yaml = new Dump(settings);
        var writer = new MyStreamToStringWriter();
        yaml.dumpNode(root, writer);
        Files.write(Paths.get(filename), writer.toString().getBytes());
    }

}
