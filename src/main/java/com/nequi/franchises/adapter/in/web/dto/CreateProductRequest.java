package com.nequi.franchises.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateProductRequest(

        @NotBlank(message = "El nombre del producto es obligatorio")
        @Size(
                max = 100,
                message = "El nombre del producto no puede superar los 100 caracteres"
        )
        String name,

        @NotNull(message = "La cantidad de stock es obligatoria")
        @PositiveOrZero(message = "El stock no puede ser negativo")
        Integer stock

) {

    public CreateProductRequest {
        if (name != null) {
            name = name.strip();
        }
    }
}