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
                    "Branch id is required"
            );
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Branch name is required"
            );
        }

        name = name.strip();

        if (name.length() > 100) {
            throw new IllegalArgumentException(
                    "Branch name must not exceed 100 characters"
            );
        }

        if (products == null) {
            throw new IllegalArgumentException(
                    "Branch products are required"
            );
        }

        products = List.copyOf(products);
    }
}