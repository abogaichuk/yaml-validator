//package com.example.yamlvalidator.rules;
//
//import com.example.yamlvalidator.entity.StringParameter;
//import com.example.yamlvalidator.entity.ValidationResult;
//
//import java.util.stream.Stream;
//
//import static com.example.yamlvalidator.entity.ValidationResult.invalid;
//import static com.example.yamlvalidator.entity.ValidationResult.valid;
//import static com.example.yamlvalidator.utils.PadmGrammar.OR_KEYWORD;
//import static com.example.yamlvalidator.utils.PadmGrammar.standardTypes;
//import static com.example.yamlvalidator.utils.ValidatorUtils.toErrorMessage;
//
//public class TypeParameterRule implements Rule {
//    private static final String UNKNOWN_TYPE = "Type is not define";
//
//    @Override
//    public ValidationResult validate(StringParameter parameter) {
//        return parameter.isTypeOrCustomParam() ? typeValidation(parameter) : valid();
//    }
//
//    private ValidationResult typeValidation(final StringParameter parameter) {
//        return Stream.of(parameter.getValue().split(OR_KEYWORD))
//                .map(String::trim)
//                .filter(partedType -> isWrong(parameter, partedType))
//                .map(type -> invalid(toErrorMessage(parameter, type, UNKNOWN_TYPE)))
//                .reduce(ValidationResult::merge)
//                .orElseGet(ValidationResult::valid);
//    }
//
//    private boolean isWrong(final StringParameter parameter, final String type) {
//        return isNotAStandardType(type) && isNotACustomType(parameter, type);
//    }
//
//    private boolean isNotACustomType(final StringParameter parameter, final String type) {
//        return !isCustomType(parameter, type);
//    }
//
//    private boolean isCustomType(final StringParameter parameter, final String type) {
//        return parameter.getRoot().getCustomTypes().stream()
//                .anyMatch(t -> t.equalsIgnoreCase(type));
//    }
//
//    private boolean isNotAStandardType(String type) {
//        return !isStandardType(type);
//    }
//
//    private boolean isStandardType(String type) {
//        return standardTypes.stream()
//                .anyMatch(t -> t.equalsIgnoreCase(type));
//    }
//}
