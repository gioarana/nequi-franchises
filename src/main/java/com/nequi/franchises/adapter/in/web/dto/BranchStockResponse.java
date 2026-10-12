package com.nequi.franchises.adapter.in.web.dto;

import com.nequi.franchises.application.model.BranchStockSummary;

import java.util.UUID;

public record BranchStockResponse(
        UUID branchId,
        String branchName,
        UUID productId,
        String productName,
        int stock
) {
    public static BranchStockResponse from(BranchStockSummary summary) {
        return new BranchStockResponse(
                summary.branchId(),
                summary.branchName(),
                summary.productId(),
                summary.productName(),
                summary.stock()
        );
    }
}