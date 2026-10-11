package com.nequi.franchises.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateProductRequest(

        @NotBlank(message = "Product name is required")
        @Size(
                max = 100,
                message = "Product name must not exceed 100 characters"
        )
        String name,

        @NotNull(message = "Product stock is required")
        @PositiveOrZero(message = "Product stock must not be negative")
        Integer stock

) {

    public CreateProductRequest {
        if (name != null) {
            name = name.strip();
        }
    }
}