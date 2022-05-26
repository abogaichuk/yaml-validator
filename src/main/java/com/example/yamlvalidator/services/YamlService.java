package com.example.yamlvalidator.services;

import com.example.yamlvalidator.MyStreamToStringWriter;
import com.example.yamlvalidator.entity.*;
import com.example.yamlvalidator.utils.ValidatorUtils;

import java.util.*;

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

@Component
public class YamlService {
    @Autowired
    private ValidationServiceImpl validationService;

    public void execute(String definitionFile, String resourceFile) throws IOException {
        try {
            var defNode = readFile(definitionFile);
            var resourceNode = readFile(resourceFile);

            ValidationResult result = defNode
                    .map(root -> new Mapper().map(root))
                    .map(schema -> validationService.validate(schema, Collections.emptyList()))
                    .orElse(ValidationResult.valid());
            System.out.println(result.getReasons());

//            List<Parameter> resources = resourceNode
//                    .map(root -> new YamlMapper().toResources(root))
//                    .orElseGet(Collections::emptyList);
//
//            ValidationResult result = defNode
//                    .map(rootDefNode -> new YamlMapper().toDefinition(rootDefNode))
//                    .map(definition -> validationService.validate(definition, resources))
//                    .orElseGet(ValidationResult::valid);
//            result.getReasons().forEach(System.out::println);

//            save(defNode.get(), "definition1.yaml");
//            save(resource.get(), "resource1.yaml");
        } catch (ParserException | ScannerException e) {
            e.getProblemMark().ifPresent(problemMark -> System.out.println(ValidatorUtils.toErrorMessage(e.getProblem(), problemMark)));
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

    public void updateNodeByPath(Node node, String path, String newValue) {
        List<String> paths = Arrays.asList(path.split("/"));
        List<NodeTuple> nodeList = new ArrayList<>();
        Iterator<String> iterator = paths.iterator();
        Optional<NodeTuple> proxy = Optional.empty();
        Node rootNode = node;
        while (iterator.hasNext()) {
            String currentTag = iterator.next();
            proxy = ((MappingNode) rootNode).getValue().stream().filter(t -> getKeyValue(t).equals(currentTag)).findFirst();
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
        newValues.forEach(value -> nodeList.add(new ScalarNode(new Tag("tag:yaml.org,2002:str"), value, ScalarStyle.PLAIN)));
        return nodeList;
    }

    private String getKeyValue(NodeTuple tuple) {
        return ((ScalarNode) tuple.getKeyNode()).getValue();
    }
}
