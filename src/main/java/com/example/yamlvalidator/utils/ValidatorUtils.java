package com.example.yamlvalidator.utils;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.StringParameter;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.text.MessageFormat.*;

public class ValidatorUtils {
    public static final String MIN_IS_NAN = "Min is not an number";
    public static final String MAX_IS_NAN = "Max is not an number";
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
//    public static final String UNKNOWN_TYPE = "Type is not define";
    public static final String DEFAULT_IS_NOT_NUMBER = "Default is not a number";
    public static final String DEFAULT_IS_NOT_DATETIME = "Default is not a datetime";
    public static final String DEFAULT_IS_NOT_BOOL = "Default is not a boolean";

//    public static final String DEFAULT = "Default";
//    public static final String TYPE = "Type";
//    public static final String FORMAT = "Format";
//    public static final String VALIDATOR = "Validators";
//    public static final String VALIDATOR_MIN = "Validators/Min";
//    public static final String VALIDATOR_MAX = "Validators/Max";
//    public static final String VALIDATOR_AFTER = "Validators/After";
//    public static final String VALIDATOR_BEFORE = "Validators/Before";
//    public static final String VALIDATOR_LIST = "List";
//    public static final String MIN = "Min";
//    public static final String MAX = "Max";

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Pattern pattern = Pattern.compile(".*?\\$\\{(\\w+)\\}.*?");

    public static boolean canBeParsedToInt(StringParameter intParam) {
        return toInt(intParam).isPresent();
    }

    public static Optional<Integer> toInt(StringParameter parameter) {
        var value = getValue(parameter);
        try {
            return Optional.of(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static <T> boolean compare(StringParameter p1, StringParameter p2,
                                      Function<StringParameter, Optional<T>> parser,
                                      BiPredicate<T, T> comparator) {
        return parser.apply(p1)
            .map(v1 -> parser.apply(p2)
                .map(v2 -> comparator.test(v1, v2))
                .orElse(Boolean.FALSE))
            .orElse(Boolean.FALSE);
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

    public static <T> boolean canBeParsed(Parameter param, Function<StringParameter, Optional<T>> parser) {
        return param instanceof StringParameter && parser.apply((StringParameter) param).isPresent();
    }

//    public static <T> boolean canBeParsed(StringParameter param, Function<StringParameter, Optional<T>> parser) {
//        return parser.apply(param).isPresent();
//    }

    public static boolean canBeParsedToDatetime(StringParameter datetimeParam) {
        return toDatetime(datetimeParam).isPresent();
    }

    public static Optional<LocalDateTime> toDatetime(StringParameter parameter) {
        var value = getValue(parameter);
        try {
            return Optional.of(LocalDateTime.parse(value, formatter));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    public static Optional<Boolean> toBoolean(StringParameter parameter) {
        var value = getValue(parameter);
        return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value) ? Optional.of(Boolean.parseBoolean(value)) : Optional.empty();
    }

    public static String getValue(Parameter p) {
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
