package com.example.customermanagement.validation;

import com.example.customermanagement.annotation.ValidDate;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public class DateValidator implements ConstraintValidator<ValidDate, String> {

    @Override
    public boolean isValid(String dateStr, ConstraintValidatorContext context) {
        if (dateStr == null) {
            return true;
        }
        return Optional.of(dateStr)
                .filter(str -> !str.isBlank())
                .flatMap(this::parseDate)
                .map(date -> !date.isAfter(LocalDate.now()))
                .orElse(false);
    }

    private Optional<LocalDate> parseDate(String dateStr) {
        try {
            var parsedDate = LocalDate.parse(dateStr);
            return Optional.of(parsedDate);
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }
}
