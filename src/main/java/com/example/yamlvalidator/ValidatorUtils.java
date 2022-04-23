package com.example.yamlvalidator;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.StringParameter;

import java.text.MessageFormat;
import java.util.Optional;

import static java.text.MessageFormat.*;

public class ValidatorUtils {
    public static final String MIN_IS_NAN = "Validator.Min is not an number";
    public static final String MAX_IS_NAN = "Validator.Max is not an number";
    public static final String MAX_LESS_THAN_MIN = "Validator.Max < Validators.Min";
    public static final String DEFAULT_LESS_THAN_MIN = "Default < Validators.Min";
    public static final String DEFAULT_MORE_THAN_MAX = "Default > Validators.Max";
    public static final String DEFAULT_WRONG = "Validators.List doesn't contain Default value";
    public static final String HAS_DUPLICATES = "ObjectParam has duplicates";

    public static final String DEFAULT = "Default";
    public static final String TYPE = "Type";
    public static final String FORMAT = "Format";
    public static final String VALIDATOR = "Validators";
    public static final String VALIDATOR_MIN = "Validators/Min";
    public static final String VALIDATOR_MAX = "Validators/Max";
    public static final String VALIDATOR_LIST = "List";
    public static final String MIN = "Min";
    public static final String MAX = "Max";

//    public static Optional<? extends Parameter> findChild(String name, ObjectParameter parameter) {
//        return isNotEmpty(name) ? parameter.getChildren().stream()
//            .filter(param -> name.equals(param.getName()))
//            .findAny() : Optional.empty();
//    }
//
//    public static Optional<? extends Parameter> findValidatorByName(String name, ObjectParameter parameter) {
//        return isEmpty(name) ? Optional.empty() :
//            findChild(VALIDATOR, parameter)
//                .flatMap(p -> findChild(name, (ObjectParameter) p));
//    }

    //todo fix
    public static boolean canBeParsedToInt(StringParameter intParam) {
        return toInt(intParam) != -1;
    }

    public static int toInt(StringParameter parameter) {
        String value = getValue(parameter);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public static String getValue(Parameter p) {
        return ((StringParameter) p).getValue();
    }

    public static boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public static boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }

    public static String toErrorMessage(Parameter p, String message) {
        return format("{0} paramname: {1} (row #{2})", message, p.getName(), p.getRow());
    }
}
