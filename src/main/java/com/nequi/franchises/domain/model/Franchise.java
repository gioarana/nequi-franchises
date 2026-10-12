package com.nequi.franchises.domain.model;

import java.util.List;
import java.util.UUID;

public record Franchise(
        UUID id,
        String name,
        List<Branch> branches
) {

    public Franchise {
        if (id == null) {
            throw new IllegalArgumentException(
                    "El ID de la franquicia es obligatorio"
            );
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre de la franquicia es obligatorio"
            );
        }

        name = name.strip();

        if (name.length() > 100) {
            throw new IllegalArgumentException(
                    "El nombre de la franquicia no puede superar los 100 caracteres"
            );
        }

        if (branches == null) {
            throw new IllegalArgumentException(
                    "La lista de sucursales de la franquicia es obligatoria"
            );
        }

        branches = List.copyOf(branches);
    }
}