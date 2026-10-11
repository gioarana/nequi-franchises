package com.nequi.franchises.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateStockRequest(

        @NotNull(message = "La cantidad de stock es obligatoria")
        @PositiveOrZero(message = "La cantidad de stock no puede ser negativa")
        Integer stock

) {
}