package com.nequi.franchises.application.model;

import java.util.UUID;

public record BranchStockSummary(UUID branchId, String branchName, UUID productId, String productName, int stock) {
}