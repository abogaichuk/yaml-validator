package com.example.yamlvalidator.rules;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.StringParameter;
import com.example.yamlvalidator.entity.ValidationResult;

import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.example.yamlvalidator.entity.ValidationResult.*;
import static com.example.yamlvalidator.rules.Conditions.*;
import static com.example.yamlvalidator.rules.ParameterRuleFactory.*;
import static com.example.yamlvalidator.utils.ValidatorUtils.*;

public class PadmGrammar {
    public static final String OR_TYPE_SPLITTER = " or ";

    public enum StandardType implements ParameterRule<ObjectParameter> {
        OBJECT(() -> custom()),
        STRING(() -> strings()),
        DATETIME(() -> datetime()),
        NUMBER(() -> numbers()),
        BOOLEAN(() -> booleans()),
        SECRET(() -> secrets());

        private final Supplier<ParameterRule<ObjectParameter>> supplier;
        StandardType(Supplier<ParameterRule<ObjectParameter>> supplier) {
            this.supplier = supplier;
        }

        @Override
        public ValidationResult validate(ObjectParameter parameter) {
            return supplier.get().validate(parameter);
        }
    }

    public enum KeyWord {
        TYPE(KeyWordType.STRING),
        ITEMS(KeyWordType.OBJECT),
        ENUM(KeyWordType.OBJECT),
        ONEOF(KeyWordType.OBJECT),
        ANYOF(KeyWordType.OBJECT),
        PROPERTIES(KeyWordType.OBJECT),
        PATTERN(KeyWordType.STRING),
        DESCRIPTION(KeyWordType.STRING),
        DEFAULT(KeyWordType.STRING),
        REQUIRED(KeyWordType.STRING),
        EXAMPLE(KeyWordType.STRING),
        BYPASS(KeyWordType.STRING),
        MIN(KeyWordType.STRING),
        MAX(KeyWordType.STRING),
        LIST(KeyWordType.OBJECT),
        AFTER(KeyWordType.STRING),
        BEFORE(KeyWordType.STRING);

        public final KeyWordType paramType;
        KeyWord(KeyWordType paramType) {
            this.paramType = paramType;
        }

//        @Override
//        public ValidationResult validate(Parameter parameter) {
//            return paramType.predicate.test(parameter) ? valid() : invalid(toErrorMessage(parameter, paramType.message));
//        }
    }

    public enum KeyWordType implements ParameterRule {
        STRING(STRING_KEYWORD, isStringParameter),
        OBJECT(OBJECT_KEYWORD, isObjectParameter);

        public final String message;
        public final Predicate<Parameter> predicate;
        KeyWordType(String message, Predicate<Parameter> predicate) {
            this.message = message;
            this.predicate = predicate;
        }

        @Override
        public ValidationResult validate(Parameter parameter) {
            return predicate.test(parameter) ? valid() : invalid(toErrorMessage(parameter, message));
        }
    }
}
