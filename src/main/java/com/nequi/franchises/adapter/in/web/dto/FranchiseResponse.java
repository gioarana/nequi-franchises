package com.nequi.franchises.adapter.in.web.dto;

import com.nequi.franchises.domain.model.Franchise;

import java.util.UUID;

public record FranchiseResponse(UUID id, String name) {

    public static FranchiseResponse from(Franchise franchise) {
        return new FranchiseResponse(
                franchise.id(),
                franchise.name()
        );
    }
}