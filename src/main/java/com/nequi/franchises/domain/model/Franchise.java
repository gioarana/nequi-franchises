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
                    "Franchise id is required"
            );
        }

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Franchise name is required"
            );
        }

        name = name.strip();

        if (name.length() > 100) {
            throw new IllegalArgumentException(
                    "Franchise name must not exceed 100 characters"
            );
        }

        if (branches == null) {
            throw new IllegalArgumentException(
                    "Franchise branches are required"
            );
        }

        branches = List.copyOf(branches);
    }
}