package com.nequi.franchises.domain.model;

import java.util.List;
import java.util.UUID;

public record Branch(
        UUID id,
        String name,
        List<Product> products
) {

    public Branch {
        if (id == null) {
            throw new IllegalArgumentException(
                    "El identificador de la sucursal es obligatorio"
            );
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre de la sucursal es obligatorio"
            );
        }

        name = name.strip();

        if (name.length() > 100) {
            throw new IllegalArgumentException(
                    "El nombre de la sucursal no puede superar los 100 caracteres"
            );
        }

        if (products == null) {
            throw new IllegalArgumentException(
                    "La lista de productos de la sucursal es obligatoria"
            );
        }

        products = List.copyOf(products);
    }
}