package com.example.yamlvalidator.utils;

public class MessagesUtils {
    public static final String MESSAGE_IS_NAN = "%1$s is not a number";
    public static final String MESSAGE_IS_NOT_A_BOOLEAN = "%1$s is not a boolean";
    public static final String MESSAGE_IS_NOT_A_DATETIME = "%1$s is not a datetime";
    public static final String MESSAGE_LESS_THAN = "%1$s < %2$s";
    public static final String MESSAGE_MORE_THAN = "%1$s > %2$s";
    public static final String MESSAGE_IS_BEFORE = "%1$s is before %2$s";
    public static final String MESSAGE_IS_AFTER = "%1$s is after %2$s";
    public static final String MESSAGE_LIST_DOES_NOT_CONTAIN = "%1$s is not in validator %2$s";
    public static final String MESSAGE_HAS_DUPLICATES = "%1$s has duplicates: %2$s";
    public static final String MESSAGE_UNKNOWN_TYPE = "%1$s, type %2$s is not define";
    public static final String MESSAGE_SCHEMA_INCORRECT = "%1$s incorrect fields %2$s";
    public static final String MESSAGE_RESOURCE_UNKNOWN_TYPE = "%1$s, value %2$s cant be resolved to type %3$s";
    public static final String MESSAGE_PARAMETER_BYPASS = "Parameter %1$s is bypass, validation is skipped, %2$s";
    public static final String MESSAGE_INVALID_RESOURCE = "%s resource is invalid";
    public static final String DATETIME_PARSED_ERROR = "Can't parse parameter %1$s using pattern %2$s";

    public static final String MANDATORY_PARAMETER = "{0} is mandatory, but missed";
    public static final String MANDATORY_CUSTOM_CHILDREN = "{0} is mandatory, and must have at least one custom field: {1}";


    public static String getMessage(String code) {
        return getMessage(code, (Object) null);
    }

    public static String getMessage(String code, Object... arguments) {
        return String.format(code, arguments);
    }
}
