package com.example.yamlvalidator.utils;

import com.example.yamlvalidator.entity.Param;

import com.example.yamlvalidator.errors.ValidationError;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.snakeyaml.engine.v2.exceptions.Mark;

import static java.text.MessageFormat.format;

public final class ValidatorUtils {
    public static final String MESSAGE_IS_NAN = "rule.is.nan";
    public static final String MESSAGE_IS_NOT_A_BOOLEAN = "rule.is.not.a.boolean";
    public static final String MESSAGE_IS_NOT_A_DATETIME = "rule.is.not.a.datetime";
    public static final String MESSAGE_LESS_THAN = "rule.less.than";
    public static final String MESSAGE_MORE_THAN = "rule.more.than";
    public static final String MESSAGE_IS_BEFORE = "rule.is.before";
    public static final String MESSAGE_IS_AFTER = "rule.is.after";
    public static final String MESSAGE_LIST_DOES_NOT_CONTAIN = "rule.list.does.not.contain";
    public static final String MESSAGE_HAS_DUPLICATES = "rule.has.duplicates";
    public static final String MESSAGE_UNKNOWN_TYPE = "rule.unknown.type";
    public static final String MESSAGE_RESOURCE_UNKNOWN_TYPE = "rule.unknown.resource.type";
    public static final String MESSAGE_PARAMETER_BYPASS = "rule.parameter.bypass";
    public static final String STRING_KEYWORD = "rule.string.keyword";
    public static final String OBJECT_KEYWORD = "rule.object.keyword";
    public static final String DATETIME_PARSED_ERROR = "rule.datetime.parsed.error";

    public static final String MANDATORY_PARAMETER = "rule.mandatory.parameter";

    public static final String OR_TYPE_SPLITTER = " or ";

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Pattern pattern = Pattern.compile(".*?\\$\\{(\\w+)\\}.*?");

    private ValidatorUtils() {}

    public static <T> boolean compare(Param p1, Param p2,
                                      Function<Param, Optional<T>> parser,
                                      BiPredicate<T, T> comparator) {
        return parser.apply(p1)
            .map(v1 -> parser.apply(p2)
                .map(v2 -> comparator.test(v1, v2))
                .orElse(Boolean.FALSE))
            .orElse(Boolean.FALSE);
    }

    public static <T> boolean contains(Param p1, Param p2,
                                       Function<Param, List<T>> listParser,
                                       Function<Param, Optional<T>> paramParser,
                                       BiPredicate<List<T>, T> predicate) {
        return paramParser.apply(p2)
                .map(value -> predicate.test(listParser.apply(p1), value))
                .orElse(Boolean.FALSE);
    }

    public static Optional<Integer> toInt(final Param parameter) {
        try {
            var value = getValue(parameter);
            return Optional.of(Integer.parseInt(value));
        } catch (NumberFormatException | ClassCastException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static Optional<String> toString(final Param parameter) {
        try {
            return Optional.of(getValue(parameter));
        } catch (ClassCastException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static List<String> toList(final Param parameter) {
        try {
             return  parameter.getChildren().stream()
                    .map(Param::getValue)
                    .collect(Collectors.toList());
        } catch (ClassCastException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public static Optional<LocalDateTime> toDatetime(final Param parameter) {
        try {
            var value = getValue(parameter);
            return Optional.of(LocalDateTime.parse(value, formatter));
        } catch (DateTimeParseException | ClassCastException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static Optional<LocalDateTime> toDatetime(final Param patternParam, final Param parameter) {
        try {
            var patternValue = getValue(patternParam);
            var value = getValue(parameter);
            return Optional.of(LocalDateTime.parse(value, DateTimeFormatter.ofPattern(patternValue)));
        } catch (DateTimeParseException | ClassCastException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }


    public static Optional<Boolean> toBoolean(final Param parameter) {
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
    public static String getValue(final Param p) {
        Objects.requireNonNull(p);
        return p.getValue();
    }

    public static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }

    public static String toErrorMessage(Param p, String message) {
//        message = format(message, p.getName());
        return format("{0}, paramname: {1} (row #{2})", message, p.getPath(), p.getRow());
    }

    public static String toErrorMessage(Param p1, Param p2, String message) {
        message = format(message, p1.getName(), p2.getName());
        return format("{0}, paramname: {1} (row #{2}), paramname: {3} (row #{4})",
                message, p1.getPath(), p1.getRow(), p2.getPath(), p2.getRow());
    }

//    public static String toErrorMessage(Param p, String message, String ... params) {
//        message = format(message, p.getName(), params);
//        return format("{0}, paramname: {1} (row #{2})", message, p.getPath(), p.getRow());
//    }

    public static String toErrorMessage(Param p, String incorrectValue, String message) {
        message = format(message, p.getName(), incorrectValue);
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
