package com.nequi.franchises.domain.model;

import java.util.UUID;

public record Product(UUID id, String name, int stock) {

    public Product {
        if (id == null) {
            throw new IllegalArgumentException(
                    "Product id is required"
            );
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Product name is required"
            );
        }

        name = name.strip();

        if (name.length() > 100) {
            throw new IllegalArgumentException(
                    "Product name must not exceed 100 characters"
            );
        }

        if (stock < 0) {
            throw new IllegalArgumentException(
                    "Product stock must not be negative"
            );
        }
    }
}