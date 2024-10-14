package com.example.customermanagement.dto;

import com.example.customermanagement.annotation.ValidDate;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerDTO {
    private Long id;

    @NotNull(message = "Name cannot be null")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Age cannot be null")
    @Positive(message = "Age must be a positive number")
    private Integer age;

    @NotNull(message = "Date of birth cannot be null")
    @ValidDate
    private String dateOfBirth;

    @Size(max = 200, message = "Address must not exceed 200 characters or null")
    private String address;

    @Pattern(regexp = "[MF]", message = "Gender must be 'M' (Male), 'F' (Female), or null")
    private String gender;
}
