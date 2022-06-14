package com.example.yamlvalidator.services;

import com.example.yamlvalidator.MyStreamToStringWriter;
import com.example.yamlvalidator.entity.*;
import com.example.yamlvalidator.errors.PadmGrammarException;
import com.example.yamlvalidator.grammar.RuleService;
import com.example.yamlvalidator.utils.ValidatorUtils;

import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.composer.Composer;
import org.snakeyaml.engine.v2.exceptions.ParserException;
import org.snakeyaml.engine.v2.exceptions.ScannerException;
import org.snakeyaml.engine.v2.nodes.MappingNode;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.nodes.NodeTuple;
import org.snakeyaml.engine.v2.nodes.ScalarNode;
import org.snakeyaml.engine.v2.nodes.SequenceNode;
import org.snakeyaml.engine.v2.nodes.Tag;
import org.snakeyaml.engine.v2.parser.ParserImpl;
import org.snakeyaml.engine.v2.scanner.StreamReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.example.yamlvalidator.utils.ValidatorUtils.nodeToString;
import static com.example.yamlvalidator.utils.ValidatorUtils.printPreview;

@Component
public class YamlService {
    private final RuleService rules = new RuleService();
    @Autowired
    private SchemaMapper schemaMapper;

    public void execute(Execution execution) throws IOException {
        try {
            var defNode = readFile(execution.getDefinition());
            var resourceNode = readFile(execution.getResource());

            var optionalResource = resourceNode
                    .map(resNode -> new ResourceMapper(new PlaceHolderResolver()).map(resNode));
            optionalResource.ifPresent(resource -> printPreview(new ResourceMapper(new PlaceHolderResolver()).map(resource)));

            defNode
                    .map(node -> schemaMapper.map(node))
                    .ifPresent(schema -> {
                        printPreview(schemaMapper.map(schema));
//                        schema.print();
                        var validationResult = schema.validate(rules, optionalResource.orElse(null));
                        validationResult.getReasons().forEach(System.out::println);
                    });
//
//            System.out.println(preview(resourceString, true));
//            System.out.println(preview(resourceString, false));

//            save("resource1.yaml", resourceString);
//            save("definition1.yaml", schemaString);
        } catch (ParserException | ScannerException e) {
            e.getProblemMark().ifPresent(
                    problemMark -> System.out.println(ValidatorUtils.toErrorMessage(e.getProblem(), problemMark)));
        } catch (PadmGrammarException pe) {
            System.out.println("error: " + pe.getMessage());
        }
    }

//    private void printPreview(Node node) {
//        try {
//            System.out.println(preview(nodeToString(node), false));
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
//    }

    private Optional<Node> readFile(String filename) throws FileNotFoundException {
        var loadSettings = LoadSettings.builder()
                .setParseComments(true)
                .build();

        var reader = new StreamReader(loadSettings, new FileReader(filename));
        var parser = new ParserImpl(loadSettings, reader);
        var composer = new Composer(loadSettings, parser);

        return composer.getSingleNode();
    }

//    private String preview(String data, boolean format) throws JsonProcessingException {
//        var yamlReader = new ObjectMapper(new YAMLFactory());
//        var obj = yamlReader.readValue(data, Object.class);
//
//        var writer = format ? new ObjectMapper() : new ObjectMapper(new YAMLFactory());
//        return writer.writeValueAsString(obj);
//    }

    private void save(String filename, String data) throws IOException {
        Files.write(Paths.get(filename), data.getBytes());
    }

//    private String nodeToString(Node root) {
//        var settings = DumpSettings.builder().build();
//        var yaml = new Dump(settings);
//        var writer = new MyStreamToStringWriter();
//        yaml.dumpNode(root, writer);
//
//        return writer.toString();
//    }

    public void updateNodeByPath(Node node, String path, String newValue) {
        List<String> paths = Arrays.asList(path.split("/"));
        List<NodeTuple> nodeList = new ArrayList<>();
        Iterator<String> iterator = paths.iterator();
        Optional<NodeTuple> proxy = Optional.empty();
        Node rootNode = node;
        while (iterator.hasNext()) {
            String currentTag = iterator.next();
            proxy = ((MappingNode) rootNode).getValue().stream()
                    .filter(t -> getKeyValue(t).equals(currentTag))
                    .findFirst();
            if (proxy.isPresent() && proxy.get().getValueNode() instanceof MappingNode) {
                rootNode = proxy.get().getValueNode();
                nodeList.add(proxy.get());
            }
            if (proxy.isPresent() && paths.size() == 1) {
                nodeList.add(proxy.get());
            }
        }
        proxy.ifPresent(tuple -> updateNode(nodeList, tuple, newValue, node));
    }

    private void updateNode(List<NodeTuple> nodes, NodeTuple tuple, String newValue, Node rootNode) {
        List<NodeTuple> root = ((MappingNode) rootNode).getValue();
        NodeTuple updatedValue = updateValue(newValue, tuple);
        ListIterator<NodeTuple> iterator = nodes.listIterator(nodes.size());
        NodeTuple node = null;
        while (iterator.hasPrevious()) {
            NodeTuple currentNode = iterator.previous();
            if (currentNode.getValueNode() instanceof MappingNode) {
                ((MappingNode) currentNode.getValueNode())
                    .getValue()
                    .set(((MappingNode) currentNode.getValueNode()).getValue().indexOf(tuple), updatedValue);
                node = currentNode;
                tuple = currentNode;
                updatedValue = currentNode;
            } else {
                root.set(root.indexOf(tuple), updatedValue);
                return;
            }
        }
        root.set(root.indexOf(node), node);
    }

    private NodeTuple updateValue(String newValue, NodeTuple section) {
        NodeTuple nodeTuple;
        if (section.getValueNode() instanceof SequenceNode) {
            nodeTuple = updateSequenceValue(newValue, section);
        } else {
            nodeTuple = updateScalarValue(newValue, section);
        }
        return nodeTuple;
    }

    private NodeTuple updateScalarValue(String newValue, NodeTuple section) {
        ScalarNode key = (ScalarNode) section.getKeyNode();
        ScalarNode value = (ScalarNode) section.getValueNode();
        ScalarNode updatedValue = new ScalarNode(
            value.getTag(),
            true,
            newValue,
            value.getScalarStyle(),
            value.getStartMark(),
            value.getEndMark()
        );
        updatedValue.setBlockComments(value.getBlockComments());
        updatedValue.setInLineComments(value.getInLineComments());
        updatedValue.setEndComments(value.getEndComments());
        return new NodeTuple(key, updatedValue);
    }

    private NodeTuple updateSequenceValue(String newValue, NodeTuple section) {
        List<String> newValues = Arrays.asList(newValue.split(" "));
        ScalarNode key = (ScalarNode) section.getKeyNode();
        SequenceNode value = (SequenceNode) section.getValueNode();
        SequenceNode updatedValue = new SequenceNode(
            value.getTag(),
            true,
            createSequenceValueList(newValues),
            FlowStyle.BLOCK,
            value.getStartMark(),
            value.getEndMark()
        );
        return new NodeTuple(key, updatedValue);
    }

    private List<Node> createSequenceValueList(List<String> newValues) {
        List<Node> nodeList = new ArrayList<>();
        newValues.forEach(value -> nodeList.add(
                new ScalarNode(new Tag("tag:yaml.org,2002:str"), value, ScalarStyle.PLAIN)));
        return nodeList;
    }

    private String getKeyValue(NodeTuple tuple) {
        return ((ScalarNode) tuple.getKeyNode()).getValue();
    }
}
