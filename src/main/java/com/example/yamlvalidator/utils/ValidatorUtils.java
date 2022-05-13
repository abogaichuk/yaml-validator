package com.example.yamlvalidator.utils;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.StringParameter;

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

import static com.example.yamlvalidator.rules.PadmGrammar.*;
import static java.text.MessageFormat.*;

public class ValidatorUtils {
    public static final String MIN_IS_NAN = "Min is not a number";
    public static final String MAX_IS_NAN = "Max is not a number";
    public static final String DEFAULT_IS_NAN = "Default is not a number";
    public static final String DEFAULT_IS_NOT_A_BOOLEAN = "Default is not a boolean";
    public static final String DEFAULT_IS_NOT_A_DATETIME = "Default is not a datetime";
    public static final String MAX_LESS_THAN_MIN = "Max < Min";
    public static final String DEFAULT_LESS_THAN_MIN = "Default < Min";
    public static final String DEFAULT_MORE_THAN_MAX = "Default > Max";
    public static final String DEFAULT_IS_BEFORE_AFTER = "Default before After";
    public static final String DEFAULT_IS_AFTER_BEFORE = "Default after Before";
    public static final String DEFAULT_WRONG = "List doesn't contains Default value";
    public static final String HAS_DUPLICATES = "ObjectParam has duplicates";
    public static final String AFTER_IS_NOT_DATETIME = "After is not a datetime";
    public static final String BEFORE_IS_NOT_DATETIME = "Before is not a datetime";
    public static final String BEFORE_DATE_IS_AFTER = "After is before Before";
    public static final String UNKNOWN_TYPE = "Type is not define";
    public static final String PARAMETER_BYPASS = "Parameter Bypass, validation is skipped";
    public static final String DEFAULT_IS_NOT_NUMBER = "Default is not a number";
    public static final String DEFAULT_IS_NOT_DATETIME = "Default is not a datetime";
    public static final String DEFAULT_IS_NOT_BOOL = "Default is not a boolean";
    public static final String WRONG_KEYWORD = "Wrong keyword type";
    public static final String STRING_KEYWORD = "Keyword must be a string";
    public static final String OBJECT_KEYWORD = "Keyword must be an object";

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Pattern pattern = Pattern.compile(".*?\\$\\{(\\w+)\\}.*?");

    public static boolean canBeParsedToInt(StringParameter intParam) {
        return toInt(intParam).isPresent();
    }

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
//        return parser.apply(p2)
//                .map(value -> listParser.apply(p1).contains(value))
//                .orElse(Boolean.FALSE);
    }

    public static boolean compareInts(StringParameter minP, StringParameter maxP) {
        return toInt(minP)
            .map(min -> toInt(maxP)
                .map(max -> min > max)
                .orElse(Boolean.FALSE))
            .orElse(Boolean.FALSE);
    }

    public static boolean compareDates(StringParameter beforeP, StringParameter afterP) {
        return toDatetime(beforeP)
            .map(before -> toDatetime(afterP)
                .map(before::isAfter)
                .orElse(Boolean.FALSE))
            .orElse(Boolean.FALSE);
    }

    public static <T> boolean canBeParsed(Parameter p, Function<Parameter, Optional<T>> parser) {
        return parser.apply(p).isPresent();
    }

    public static boolean canBeParsedToDatetime(final StringParameter datetimeParam) {
        return toDatetime(datetimeParam).isPresent();
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
            return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value) ? Optional.of(Boolean.parseBoolean(value)) : Optional.empty();
        } catch (ClassCastException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

//    public static Optional<String> findWrongType(final Parameter parameter) {
//        try {
//            var value = getValue(parameter);
//            return Stream.of(value.split(OR_KEYWORD))
//                    .map(String::trim)
//                    .filter(partedType -> isNotAType(parameter, partedType))
//                    .findAny();
//        } catch (ClassCastException e) {
//            e.printStackTrace();
//            return Optional.of(e.getMessage());
//        }
//    }

    //todo logic for objectparam type child?!!
    public static boolean isWrongType(final StringParameter p) {
        return Stream.of(p.getValue().split(OR_TYPE_SPLITTER))
                .map(String::trim)
                .anyMatch(part -> isNotAType(p, part));
    }
    //todo logic for stringParam??!!
    public static Optional<String> getIncorrectType(final Parameter p) {
        return isKeyWord(p) ? Optional.empty() : getTypeValue(p)
                .flatMap(value -> Stream.of(value.split(OR_TYPE_SPLITTER))
                        .map(String::trim)
                        .filter(splitted -> isNotAType(p, splitted))
                        .findAny());
    }

    public static Optional<String> getTypeValue(final Parameter p) {
        if (p instanceof StringParameter) {
            return Optional.of(getValue(p));
        } else {
            return ((ObjectParameter) p).findChild(KeyWord.TYPE.name())
                    .map(ValidatorUtils::getValue);
        }
    }

    //todo refactor? inside Parameter?
    public static String getValue(final Parameter p) {
        Objects.requireNonNull(p);
        return ((StringParameter) p).getValue();
    }

    public static boolean isNotAKeyword(final Parameter p) {
        return !isKeyWord(p);
    }

    private static boolean isKeyWord(final Parameter p) {
        return Stream.of(KeyWord.values()).anyMatch(keyWord -> keyWord.name().equalsIgnoreCase(p.getName()));
    }

    private static boolean isNotAType(final Parameter parameter, final String type) {
        return isNotAStandardType(type) && isNotACustomType(parameter, type);
    }

    private static boolean isNotACustomType(final Parameter parameter, final String type) {
        return !isCustomType(parameter, type);
    }

    private static boolean isCustomType(final Parameter parameter, final String type) {
        return parameter.getRoot().getCustomTypes().stream()
                .anyMatch(t -> t.equalsIgnoreCase(type));
    }

    private static boolean isNotAStandardType(String type) {
        return !isStandardType(type);
    }

    private static boolean isStandardType(String type) {
        return Stream.of(StandardType.values())
                .anyMatch(t -> t.name().equalsIgnoreCase(type));
    }

    public static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }

    public static String toErrorMessage(Parameter p, String message) {
        return format("{0} paramname: {1} (row #{2})", message, p.getPath(), p.getRow());
    }

    public static String toErrorMessage(Parameter p, String incorrectValue, String message) {
        return format("{0} paramname: {1}, parameterValue: {2} (row #{3})", message, p.getPath(), incorrectValue, p.getRow());
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
