package com.example.yamlvalidator.factory;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.Parameter;
import com.example.yamlvalidator.entity.StringParameter;
import com.example.yamlvalidator.entity.ValidationResult;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

import static com.example.yamlvalidator.entity.ValidationResult.invalid;
import static com.example.yamlvalidator.utils.ValidatorUtils.toErrorMessage;

public class ListContainsRule implements Rule {
    private final String child1;
    private final String child2;
    private final String message;
    private final BiPredicate<List<String>, String> predicate;

    public ListContainsRule(String child1, String child2, String message, BiPredicate<List<String>, String> predicate) {
        this.child1 = child1;
        this.child2 = child2;
        this.message = message;
        this.predicate = predicate;
    }

    @Override
    public ValidationResult validate(Parameter parameter) {
        return extractChild((ObjectParameter) parameter)
                .map(list -> ((ObjectParameter) parameter).getChildAsString(child2)
                        .filter(p2 -> predicate.test(list, p2.getValue()))
                        .map(p2 -> invalid(toErrorMessage(p2, message)))
                        .orElseGet(ValidationResult::valid))
                .orElseGet(ValidationResult::valid);
    }

    //take Validator child List
    private Optional<List<String>> extractChild(ObjectParameter parameter) {
        return parameter.findValidatorParam(child1)
                .filter(p1 -> p1 instanceof ObjectParameter)
                .map(ObjectParameter.class::cast)
                .map(p1 -> p1.getChildren().stream()
                        .filter(sp -> sp instanceof StringParameter)
                        .map(StringParameter.class::cast)
                        .map(StringParameter::getValue)
                        .collect(Collectors.toList()));
    }
}
