package com.nequi.franchises.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBranchRequest(

        @NotBlank(message = "Branch name is required")
        @Size(
                max = 100,
                message = "Branch name must not exceed 100 characters"
        )
        String name

) {

    public CreateBranchRequest {
        if (name != null) {
            name = name.strip();
        }
    }
}