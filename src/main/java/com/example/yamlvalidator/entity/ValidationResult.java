package com.example.yamlvalidator.entity;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;

public interface ValidationResult {
    ValidationResult merge(final ValidationResult right);

    boolean isValid();

    //todo singelton
    static ValidationResult valid() {
        return new Valid();
    }

    static ValidationResult invalid(final Set<String> reasons) {
        return new Invalid(reasons);
    }

    static ValidationResult invalid(final String... reasons) {
        return new Invalid(new HashSet<>(asList(reasons)));
    }

    default Set<String> getReasons() {
        return emptySet();
    }

    class Invalid implements ValidationResult {

        private final Set<String> reasons;

        Invalid(final Set<String> reasons) {
            this.reasons = reasons;
        }

        @Override
        public Set<String> getReasons() {
            return this.reasons;
        }

        @Override
        public ValidationResult merge(final ValidationResult right) {
            final HashSet<String> mutation = new HashSet<>(reasons);
            mutation.addAll(right.getReasons());
            return ValidationResult.invalid(mutation);
        }

        @Override
        public boolean isValid() {
            return false;
        }

    }

    class Valid implements ValidationResult {
        @Override
        public ValidationResult merge(final ValidationResult right) {
            return right;
        }

        @Override
        public boolean isValid() {
            return true;
        }
    }
}
