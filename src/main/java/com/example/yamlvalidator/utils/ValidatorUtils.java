package com.example.yamlvalidator.utils;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.StringParameter;

import com.example.yamlvalidator.validators.error.ValidationError;
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
import org.snakeyaml.engine.v2.exceptions.Mark;

import static java.text.MessageFormat.format;

public final class ValidatorUtils {
    public static final String IS_NAN = "{0} is not a number";
    public static final String IS_NOT_A_BOOLEAN = "{0} is not a boolean";
    public static final String IS_NOT_A_DATETIME = "{0} is not a datetime";
    public static final String LESS_THAN = "{0} < {1}";
    public static final String MORE_THAN = "{0} > {1}";
    public static final String IS_BEFORE = "{0} is before {1}";
    public static final String IS_AFTER = "{0} is after {1}";
    public static final String DEFAULT_WRONG = "List doesn't contains Default value";
    public static final String HAS_DUPLICATES = "Parameter: {0} has duplicates";
    public static final String UNKNOWN_TYPE = "Type {0} is not define";
    public static final String PARAMETER_BYPASS = "Parameter Bypass, validation is skipped";
    public static final String STRING_KEYWORD = "Keyword {0} must be a string";
    public static final String OBJECT_KEYWORD = "Keyword {0} must be an object";

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

    public static <T> boolean contains(ObjectParameter p1, Parameter p2,
                                       Function<ObjectParameter, List<T>> listParser,
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
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static Optional<String> toString(final Parameter parameter) {
        try {
            return Optional.of(getValue(parameter));
        } catch (ClassCastException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static List<String> toList(final ObjectParameter parameter) {
        try {
             return  parameter.getChildren().stream()
                    .map(StringParameter.class::cast)
                    .map(StringParameter::getValue)
                    .collect(Collectors.toList());
        } catch (ClassCastException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public static Optional<LocalDateTime> toDatetime(final Parameter parameter) {
        try {
            var value = getValue(parameter);
            return Optional.of(LocalDateTime.parse(value, formatter));
        } catch (DateTimeParseException | ClassCastException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static Optional<Boolean> toBoolean(final Parameter parameter) {
        try {
            var value = getValue(parameter);
            return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)
                    ? Optional.of(Boolean.parseBoolean(value)) : Optional.empty();
        } catch (ClassCastException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    //todo refactor? inside Parameter?
    public static String getValue(final Parameter p) {
        Objects.requireNonNull(p);
        return ((StringParameter) p).getValue();
    }

    public static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }

    public static String toErrorMessage(Parameter p, String message) {
        message = format(message, p.getName());
        return format("{0}, paramname: {1} (row #{2})", message, p.getPath(), p.getRow());
    }

    public static String toErrorMessage(Parameter p1, Parameter p2, String message) {
        message = format(message, p1.getName(), p2.getName());
        return format("{0}, paramname: {1} (row #{2}), paramname: {3} (row #{4})",
                message, p1.getPath(), p1.getRow(), p2.getPath(), p2.getRow());
    }

    public static String toErrorMessage(Parameter p, String incorrectValue, String message) {
        message = format(message, incorrectValue);
        return format("{0}, paramname: {1} (row #{2})", message, p.getPath(), p.getRow());
    }

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
}
