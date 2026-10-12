package com.nequi.franchises.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateNameRequest(@NotBlank(message = "El nombre es obligatorio")
                                @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
                                String name) {

    public UpdateNameRequest {
        if (name != null) {
            name = name.strip();
        }
    }
}