package com.example.customermanagement.annotation;

import com.example.customermanagement.validation.DateValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateValidator.class)
public @interface ValidDate {
    String message() default "Date must be in the format yyyy-MM-dd, be a valid date, and not be in the future";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
