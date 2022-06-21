package com.example.yamlvalidator.utils;

import com.example.yamlvalidator.MyStreamToStringWriter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.errors.ValidationError;
import com.example.yamlvalidator.grammar.KeyWord;
import com.example.yamlvalidator.grammar.StandardType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.snakeyaml.engine.v2.api.Dump;
import org.snakeyaml.engine.v2.api.DumpSettings;
import org.snakeyaml.engine.v2.api.LoadSettings;
import org.snakeyaml.engine.v2.composer.Composer;
import org.snakeyaml.engine.v2.exceptions.Mark;
import org.snakeyaml.engine.v2.nodes.Node;
import org.snakeyaml.engine.v2.parser.ParserImpl;
import org.snakeyaml.engine.v2.scanner.StreamReader;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.text.MessageFormat.format;

public final class ValidatorUtils {
    public static final String OR_TYPE_SPLITTER = " or ";
    public static final String EMPTY = "";

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Pattern pattern = Pattern.compile(".*?\\$\\{(\\w+)\\}.*?");

    private ValidatorUtils() {}

    public static <T> boolean compare(Parameter p1, Parameter p2,
                                      Function<Parameter, Optional<T>> parser,
                                      BiPredicate<T, T> comparator) {
        return parser.apply(p1)
            .map(v1 -> parser.apply(p2)
                .map(v2 -> comparator.test(v1, v2))
                .orElse(Boolean.FALSE))
            .orElse(Boolean.FALSE);
    }

    public static <T> boolean contains(Parameter p1, Parameter p2,
                                       Function<Parameter, List<T>> listParser,
                                       Function<Parameter, Optional<T>> paramParser,
                                       BiPredicate<List<T>, T> predicate) {
        return paramParser.apply(p2)
                .map(value -> predicate.test(listParser.apply(p1), value))
                .orElse(Boolean.FALSE);
    }

    public static Optional<Integer> toInt(final Parameter parameter) {
        try {
            var value = getValue(parameter);
            return Optional.of(Integer.parseInt(value));
        } catch (NumberFormatException | ClassCastException e) {
//            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static Optional<String> toString(final Parameter parameter) {
        try {
            return Optional.of(getValue(parameter));
        } catch (ClassCastException e) {
//            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static List<String> toList(final Parameter parameter) {
        try {
             return  parameter.getChildren()
                    .map(Parameter::getValue)
                    .collect(Collectors.toList());
        } catch (ClassCastException e) {
//            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public static Optional<LocalDateTime> toDatetime(final Parameter parameter) {
        try {
            var value = getValue(parameter);
            return Optional.of(LocalDateTime.parse(value, formatter));
        } catch (DateTimeParseException | ClassCastException e) {
//            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static Optional<LocalDateTime> toDatetime(final Parameter patternParam, final Parameter parameter) {
        try {
            var patternValue = getValue(patternParam);
            var value = getValue(parameter);
            return Optional.of(LocalDateTime.parse(value, DateTimeFormatter.ofPattern(patternValue)));
        } catch (DateTimeParseException | ClassCastException e) {
//            e.printStackTrace();
            return Optional.empty();
        }
    }


    public static Optional<Boolean> toBoolean(final Parameter parameter) {
        try {
            var value = getValue(parameter);
            return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)
                    ? Optional.of(Boolean.parseBoolean(value)) : Optional.empty();
        } catch (ClassCastException e) {
//            e.printStackTrace();
            return Optional.empty();
        }
    }

    //todo refactor? inside Parameter?
    public static String getValue(final Parameter p) {
        Objects.requireNonNull(p);
        return p.getValue();
    }

    public static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }

//    public static String toErrorMessage(Param p, String message) {
////        message = format(message, p.getName());
//        return format("{0}, paramname: {1} (row #{2})", message, p.getPath(), p.getRow());
//    }
//
//    public static String toErrorMessage(Param p1, Param p2, String message) {
//        message = format(message, p1.getName(), p2.getName());
//        return format("{0}, paramname: {1} (row #{2}), paramname: {3} (row #{4})",
//                message, p1.getPath(), p1.getRow(), p2.getPath(), p2.getRow());
//    }
//
////    public static String toErrorMessage(Param p, String message, String ... params) {
////        message = format(message, p.getName(), params);
////        return format("{0}, paramname: {1} (row #{2})", message, p.getPath(), p.getRow());
////    }
//
//    public static String toErrorMessage(Param p, String incorrectValue, String message) {
//        message = format(message, p.getName(), incorrectValue);
//        return format("{0}, paramname: {1} (row #{2})", message, p.getPath(), p.getRow());
//    }

    public static String toErrorMessage(String problem, Mark problemMark) {
        ValidationError error = ValidationError.of(problemMark.getLine(), problemMark.getColumn(), problem);
        return format("Found problem in line: {0}, column: {1}, cause: {2}",
            error.getLine(),
            error.getColumn(),
            error.getCause());
    }

    public static String replaceHolder(String s, String placeholder) {
        var env = System.getenv(placeholder);
        var replacement = "${" + placeholder + "}";
        //todo throw an error?
        return env == null ? s.replace(replacement, placeholder) : s.replace(replacement, env);
    }

    public static String matchAndReplaceHolders(String s) {
        var matcher = pattern.matcher(s);
        while (matcher.matches()) {
            s = replaceHolder(s, matcher.group(1));
            matcher = pattern.matcher(s);
        }
        return s;
    }

    public static boolean isNotAStandardType(final String type) {
        return !isStandardType(type);
    }

    public static boolean isStandardType(String type) {
        return Stream.of(StandardType.values())
                .anyMatch(t -> t.name().equalsIgnoreCase(type));
    }

    public static boolean isTypeKeyWord(String name) {
        return KeyWord.TYPE.name().equalsIgnoreCase(name);
    }

    public static boolean isNotAKeyword(String name) {
        return getKeyWord(name).isEmpty();
    }

    public static Optional<KeyWord> getKeyWord(String name) {
        return Stream.of(KeyWord.values())
                .filter(keyWord -> keyWord.name().equalsIgnoreCase(name))
                .findAny();
    }

//    public static Position getPosition(final Node node) {
//        return Optional.ofNullable(node)
//                .flatMap(Node::getStartMark)
//                .map(mark -> Position.of(mark.getLine(), mark.getColumn()))
//                .orElse(null);
//    }
//
//    public static String getName(final Node keyNode) {
//        return Optional.ofNullable(keyNode)
//                .map(ScalarNode.class::cast)
//                .map(ScalarNode::getValue)
//                .map(String::toLowerCase)
//                .orElse(EMPTY);
//    }

    public static Optional<Node> yamlToNode(String yaml) {
        var loadSettings = LoadSettings.builder()
                .setParseComments(true)
                .build();
        var reader = new StreamReader(loadSettings, yaml);
        var parser = new ParserImpl(loadSettings, reader);
        var composer = new Composer(loadSettings, parser);

        return composer.getSingleNode();
    }

    public static void printPreview(Node node) {
        System.out.println(preview(nodeToString(node)));
    }

    public static String nodeToString(Node root) {
        var settings = DumpSettings.builder().build();
        var yaml = new Dump(settings);
        var writer = new MyStreamToStringWriter();
        yaml.dumpNode(root, writer);

        return writer.toString().trim();
    }

    public static String preview(String data) {
        try {
            var yamlReader = new ObjectMapper(new YAMLFactory());
            var obj = yamlReader.readValue(data, Object.class);
            var writer = new ObjectMapper(new YAMLFactory());
            return writer.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return EMPTY;
        }
    }
}
