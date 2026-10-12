package com.nequi.franchises.domain.model;

import java.util.UUID;

public record Product(UUID id, String name, int stock) {

    public Product {
        if (id == null) {
            throw new IllegalArgumentException(
                    "El ID del producto es obligatorio"
            );
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del producto es obligatorio"
            );
        }

        name = name.strip();

        if (name.length() > 100) {
            throw new IllegalArgumentException(
                    "El nombre del producto no puede superar los 100 caracteres"
            );
        }

        if (stock < 0) {
            throw new IllegalArgumentException(
                    "El stock no puede ser negativo"
            );
        }
    }
}