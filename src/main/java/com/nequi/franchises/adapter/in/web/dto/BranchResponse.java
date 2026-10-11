package com.nequi.franchises.adapter.in.web.dto;

import com.nequi.franchises.domain.model.Branch;

import java.util.UUID;

public record BranchResponse(
        UUID id,
        UUID franchiseId,
        String name
) {

    public static BranchResponse from(
            UUID franchiseId,
            Branch branch
    ) {
        return new BranchResponse(
                branch.id(),
                franchiseId,
                branch.name()
        );
    }
}