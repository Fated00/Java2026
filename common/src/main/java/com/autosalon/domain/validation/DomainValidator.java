package com.autosalon.domain.validation;

import com.autosalon.domain.exception.DomainValidationException;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.UUID;

public final class DomainValidator {
    private DomainValidator() {
    }

    public static UUID requireId(UUID value, String fieldName) {
        if (value == null) {
            throw new DomainValidationException(fieldName + " must be provided");
        }
        return value;
    }

    public static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new DomainValidationException(fieldName + " must be provided");
        }
        return value.trim();
    }

    public static <T> T requireNonNull(T value, String fieldName) {
        if (value == null) {
            throw new DomainValidationException(fieldName + " must be provided");
        }
        return value;
    }

    public static <T extends Collection<?>> T requireNotEmpty(T value, String fieldName) {
        requireNonNull(value, fieldName);
        if (value.isEmpty()) {
            throw new DomainValidationException(fieldName + " must not be empty");
        }
        return value;
    }

    public static int requirePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new DomainValidationException(fieldName + " must be positive");
        }
        return value;
    }

    public static BigDecimal requirePositive(BigDecimal value, String fieldName) {
        requireNonNull(value, fieldName);
        if (value.signum() <= 0) {
            throw new DomainValidationException(fieldName + " must be positive");
        }
        return value;
    }

    public static BigDecimal requireNonNegative(BigDecimal value, String fieldName) {
        requireNonNull(value, fieldName);
        if (value.signum() < 0) {
            throw new DomainValidationException(fieldName + " must be zero or positive");
        }
        return value;
    }
}
