package com.example.yamlvalidator.rules;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.StringParameter;
import com.example.yamlvalidator.entity.ValidationResult;

import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.example.yamlvalidator.rules.ParameterRuleFactory.*;
import static com.example.yamlvalidator.utils.ValidatorUtils.OBJECT_KEYWORD;
import static com.example.yamlvalidator.utils.ValidatorUtils.STRING_KEYWORD;

public class PadmGrammar {
    public static final String OR_TYPE_SPLITTER = " or ";

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
    }

    public enum KeyWordType {
        STRING(STRING_KEYWORD, p -> p instanceof StringParameter),
        OBJECT(OBJECT_KEYWORD, p -> p instanceof ObjectParameter);

        public final String message;
        public final Predicate<Parameter> predicate;
        KeyWordType(String message, Predicate<Parameter> predicate) {
            this.message = message;
            this.predicate = predicate;
        }
    }

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
}
