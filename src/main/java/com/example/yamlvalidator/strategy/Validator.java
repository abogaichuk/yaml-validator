package com.example.yamlvalidator.strategy;

import com.example.yamlvalidator.entity.ObjectParameter;
import com.example.yamlvalidator.entity.ValidationResult;
import com.example.yamlvalidator.validators.Conditions;
import com.example.yamlvalidator.validators.ParameterValidationRules;
import com.example.yamlvalidator.validators.ParameterValidator;
import com.example.yamlvalidator.validators.ValidatorPredicates;

import java.util.function.Function;

import static com.example.yamlvalidator.entity.ValidationResult.invalid;
import static com.example.yamlvalidator.entity.ValidationResult.valid;
import static com.example.yamlvalidator.utils.ValidatorUtils.HAS_DUPLICATES;
import static com.example.yamlvalidator.utils.ValidatorUtils.toErrorMessage;
import static com.example.yamlvalidator.validators.Conditions.*;
import static com.example.yamlvalidator.validators.ParameterValidator.*;

public interface Validator {
    ValidationResult validate(ObjectParameter parameter);

    static Validator noDuplicates() {
        return parameter -> hasDuplicates.test(parameter) ? invalid(toErrorMessage(parameter, HAS_DUPLICATES)) : valid();
    }

    static Validator dateTime() {
        return parameter -> afterCanBeParsed.and(beforeCanBeParsed.and(beforeIsAfter)).apply(parameter);
    }

    static Validator numbers() {
        return numbers::apply;
    }

    static Validator strings() {
        return defaultInList::apply;
    }

    static Validator of() {
        return parameter -> {
            ValidationResult result;
            if (ValidatorPredicates.isCustom.test(parameter)) {
                result = noDuplicates().validate(parameter);
            } else if (ValidatorPredicates.isDateTime.test(parameter)) {
                result = dateTime().and(noDuplicates()).validate(parameter);
            } else if (ValidatorPredicates.isString.test(parameter)) {
                result = strings().and(noDuplicates()).validate(parameter);
            } else if (ValidatorPredicates.isNumber.test(parameter)) {
                result = numbers().and(noDuplicates()).validate(parameter);
            } else {
                result = ValidationResult.valid();
            }
            return result;
        };
    }

    default Validator and(final Validator other) {
        return parameter -> {
            final ValidationResult left = this.validate(parameter);
            final ValidationResult right = other.validate(parameter);

            return left.merge(right);
        };
    }

}
