package com.example.yamlvalidator.services;

import com.example.yamlvalidator.MyStreamToStringWriter;
import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.ValidationResult;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.composer.Composer;
import org.snakeyaml.engine.v2.exceptions.ParserException;
import org.snakeyaml.engine.v2.exceptions.ScannerException;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.parser.ParserImpl;
import org.snakeyaml.engine.v2.scanner.StreamReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class YamlService {
    @Autowired
    private ValidationServiceImpl validationService;

    public void execute(String definitionFile, String resourceFile) throws IOException {
        try {
            var defNode = readFile(definitionFile);
            var resourceNode = readFile(resourceFile);

            List<Parameter> resources = resourceNode
                    .map(root -> new YamlMapper().toResources(root))
                    .orElseGet(Collections::emptyList);

            ValidationResult result = defNode
                    .map(rootDefNode -> new YamlMapper().toDefinition(rootDefNode))
                    .map(definition -> validationService.validate(definition, resources))
                    .orElseGet(ValidationResult::valid);
            result.getReasons().forEach(System.out::println);

//            save(defNode.get(), "definition1.yaml");
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
//        System.out.println(writer);
        Files.write(Paths.get(filename), writer.toString().getBytes());
    }

}
