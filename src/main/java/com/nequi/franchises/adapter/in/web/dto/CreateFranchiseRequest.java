package com.nequi.franchises.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateFranchiseRequest(
        @NotBlank(message = "El nombre de la franquicia es obligatorio")
        @Size(max = 100, message = "El nombre de la franquicia no puede superar los 100 caracteres")
        String name
) {

    public CreateFranchiseRequest {
        if (name != null) {
            name = name.strip();
        }
    }
}