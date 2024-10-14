package com.example.customermanagement.utils;

import com.example.customermanagement.exception.InvalidSortParameterException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.List;

public final class SortUtils {

    private static final List<String> ALLOWED_SORT_DIRECTIONS = Arrays.asList(
            "asc",
            "desc"
    );

    private SortUtils() {
    }

    public static Pageable createPageable(int page, int size, String sortBy, String sortDir,
                                          List<String> allowedSortFields) {
        if (!allowedSortFields.contains(sortBy)) {
            throw new InvalidSortParameterException("Invalid sort parameter '" + sortBy +
                    "'. Allowed values are " + allowedSortFields);
        }

        if (!ALLOWED_SORT_DIRECTIONS.contains(sortDir.toLowerCase())) {
            throw new InvalidSortParameterException("Invalid sort direction '" + sortDir +
                    "'. Allowed values are " + ALLOWED_SORT_DIRECTIONS);
        }

        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        Sort sort = Sort.by(direction, sortBy);
        return PageRequest.of(page, size, sort);
    }
}
