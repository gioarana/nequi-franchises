package com.nequi.franchises.adapter.in.web.dto;

import com.nequi.franchises.domain.model.Product;

import java.util.UUID;

public record ProductResponse(UUID id, UUID branchId, String name, int stock) {

    public static ProductResponse from(UUID branchId, Product product) {
        return new ProductResponse(product.id(), branchId, product.name(), product.stock());
    }
}