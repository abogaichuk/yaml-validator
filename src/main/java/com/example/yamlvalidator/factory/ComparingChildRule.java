package com.example.yamlvalidator.factory;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.StringParameter;
import com.example.yamlvalidator.entity.ValidationResult;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static com.example.yamlvalidator.entity.ValidationResult.invalid;
import static com.example.yamlvalidator.utils.ValidatorUtils.toErrorMessage;

public class ComparingChildRule implements Rule {
    private final String message;
    private final String child1;
    private final String child2;
    private final BiPredicate<StringParameter, StringParameter> comparator;

    public ComparingChildRule(String child1, String child2, String message, BiPredicate<StringParameter, StringParameter> comparator) {
        this.message = message;
        this.child1 = child1;
        this.child2 = child2;
        this.comparator = comparator;
    }

    @Override
    public ValidationResult validate(Parameter parameter) {
        return ((ObjectParameter) parameter).getChildAsString(child1)
                .map(p1 -> ((ObjectParameter) parameter).getChildAsString(child2)
                        .filter(p2 -> comparator.test(p1, p2))
                        .map(p2 -> invalid(toErrorMessage(p2, message)))
                        .orElseGet(ValidationResult::valid))
                .orElseGet(ValidationResult::valid);
    }
}
