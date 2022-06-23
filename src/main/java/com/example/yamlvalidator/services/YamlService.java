package com.example.yamlvalidator.services;

import com.example.yamlvalidator.entity.Execution;
import com.example.yamlvalidator.errors.PadmGrammarException;
import com.example.yamlvalidator.grammar.RuleService;
import com.example.yamlvalidator.mappers.SchemaMapper;
import com.example.yamlvalidator.utils.ValidatorUtils;
import org.snakeyaml.engine.v2.common.FlowStyle;
import org.snakeyaml.engine.v2.common.ScalarStyle;
import org.snakeyaml.engine.v2.exceptions.ParserException;
import org.snakeyaml.engine.v2.exceptions.ScannerException;
import org.snakeyaml.engine.v2.nodes.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class YamlService {
    private final RuleService rules = new RuleService();

    public void execute(Execution execution) throws IOException {
        try {
            var originalSchema = ValidatorUtils.read(execution.getDefinition());
            var originalResource = ValidatorUtils.read(execution.getResource());

//            var mapper = new PlaceHolderMapper(originalSchema);
//            var mapper = new PlaceHolderMapper(Schema.SchemaBuilder.builder());
            var mapper = new SchemaMapper();
//            var mapper = new PlaceHolderMapper(Resource.ResourceBuilder.builder());
            mapper.mapToParam(originalSchema)
                    .map(parameter -> {
                        System.out.println(parameter);
                        return mapper.mapToNode(parameter);
                    })
                    .map(ValidatorUtils::nodeToString)
                    .ifPresent(System.out::println);
//            MappingUtils.stringToNode(originalSchema)
//                    .map(node -> new PadmTransformer().transform())

//            PadmMapper padmMapper = new PadmMapper();
//            MappingUtils.stringToNode(originalSchema)
//                    .map(node -> padmMapper.map(node))
//                    .map(parameter -> padmMapper.mapToNode(parameter))
//                    .map(ValidatorUtils::nodeToString)
//                    .ifPresent(System.out::println);

//            Mapper mapper = new DefaultMapper();
//            Mapper mapper = new DefaultMapper();
//            mapper.mapToParam(originalSchema, new SimpleResolver())
//                    .map(parameter -> mapper.mapToNode(parameter))
//                    .map(ValidatorUtils::nodeToString)
//                    .ifPresent(System.out::println);

//            Parameter parameter = MappingUtils.map(originalSchema);
//            Node node = MappingUtils.map(parameter);
//            String yaml = ValidatorUtils.nodeToString(node);
//            System.out.println(yaml);

//            PlaceHolderResolver customTypesResolver = new PlaceHolderResolver(new ResourceMapper());
//            Optional<Parameter> resolved = customTypesResolver.resolve(originalSchema);

//            resolved
//                    .map(MappingUtils::map)
//                    .map(ValidatorUtils::nodeToString)
//                    .ifPresent(System.out::println);
//            var mapper = new ResourceMapper();
//            mapper.map(schema)
//                    .map(MappingUtils::map)
//                    .map(ValidatorUtils::nodeToString)
//                    .ifPresent(System.out::println);
//            var defNode = MappingUtils.fileToNode(execution.getDefinition());
//            var resourceNode = MappingUtils.fileToNode(execution.getResource());
//
//            var optionalResource = resourceNode
//                    .map(node -> new ResourceMapper((MappingNode) node).map());
//            optionalResource.ifPresent(resource -> printPreview(MappingUtils.map(resource)));
//
//            defNode
//                    .map(node -> new SchemaMapper((MappingNode) node).map())
//                    .ifPresent(schema -> {
//                        printPreview(MappingUtils.map(schema));
//                        var validationResult = schema.validate(rules, optionalResource.orElse(null));
//                        validationResult.getReasons().forEach(System.out::println);
//                    });
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

    private void save(String filename, String data) throws IOException {
        Files.write(Paths.get(filename), data.getBytes());
    }

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
                new ScalarNode(Tag.STR, value, ScalarStyle.PLAIN)));
        return nodeList;
    }

    private String getKeyValue(NodeTuple tuple) {
        return ((ScalarNode) tuple.getKeyNode()).getValue();
    }
}
