package com.nequi.franchises.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateFranchiseRequest(

        @NotBlank(message = "Franchise name is required")
        @Size(
                max = 100,
                message = "Franchise name must not exceed 100 characters"
        )
        String name

) {

    public CreateFranchiseRequest {
        if (name != null) {
            name = name.strip();
        }
    }
}