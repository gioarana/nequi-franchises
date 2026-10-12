package com.nequi.franchises.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBranchRequest(@NotBlank(message = "El nombre de la sucursal es obligatorio")
                                  @Size(max = 100, message = "El nombre de la sucursal no puede superar los 100 caracteres")
                                  String name) {

    public CreateBranchRequest {
        if (name != null) {
            name = name.strip();
        }
    }
}