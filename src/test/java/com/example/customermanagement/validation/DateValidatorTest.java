package com.example.customermanagement.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateValidatorTest {

    private DateValidator dateValidator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        dateValidator = new DateValidator();
        context = null;
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"2020-01-01", "0001-01-01"})
    void givenValidPastOrNullDate_whenValidated_thenReturnsTrue(String dateStr) {
        boolean result = dateValidator.isValid(dateStr, context);
        assertTrue(result, "Date \"" + dateStr + "\" should be valid");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "   ",
            "invalid-date",
            "2023-02-30",
            "9999-12-31",
            "2023-09-25T10:15:30",
            "25-09-2023"
    })
    void givenInvalidDateString_whenValidated_thenReturnsFalse(String dateStr) {
        boolean result = dateValidator.isValid(dateStr, context);
        assertFalse(result, "Date \"" + dateStr + "\" should be invalid");
    }

    @Test
    void givenFutureDate_whenValidated_thenReturnsFalse() {
        String dateStr = LocalDate.now().plusDays(1).toString();
        boolean result = dateValidator.isValid(dateStr, context);
        assertFalse(result, "Future date should be invalid");
    }

    @Test
    void givenCurrentDate_whenValidated_thenReturnsTrue() {
        String dateStr = LocalDate.now().toString();
        boolean result = dateValidator.isValid(dateStr, context);
        assertTrue(result, "Current date should be valid");
    }
}
