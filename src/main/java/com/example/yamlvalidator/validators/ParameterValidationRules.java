package com.example.yamlvalidator.validators;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.ValidationResult;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.example.yamlvalidator.validators.ParameterValidator.*;

public class ParameterValidationRules {
    private static Map<Predicate<ObjectParameter>, Function<ObjectParameter, ValidationResult>> parameterRules = new HashMap<>();

    static {
        parameterRules.put(p ->  true, noDuplicates); //all params
        parameterRules.put(ValidatorPredicates.isNumber, noDuplicates.and(numbers));
        parameterRules.put(ValidatorPredicates.isString, noDuplicates.and(defaultInList));
//        parameterRules.put(ValidatorPredicates.isDateTime, noDuplicates.and(defaultInList));
    }

    public static Function<ObjectParameter, ValidationResult> getRulesFor() {
        return parameter -> parameterRules.entrySet().stream()
            .filter(entry -> entry.getKey().test(parameter))
            .map(entry -> entry.getValue().apply(parameter))
            .findAny().orElseGet(ValidationResult::valid);
    }
}
