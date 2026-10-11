package com.nequi.franchises.adapter.in.web;

import com.nequi.franchises.adapter.in.web.dto.CreateProductRequest;
import com.nequi.franchises.adapter.in.web.dto.ProductResponse;
import com.nequi.franchises.adapter.in.web.dto.UpdateStockRequest;
import com.nequi.franchises.application.service.DeleteProductService;
import com.nequi.franchises.application.service.UpdateProductStockService;
import com.nequi.franchises.application.service.AddProductService;
import com.nequi.franchises.application.service.UpdateProductStockService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping(
        "/api/v1/franchises/{franchiseId}/branches/{branchId}/products"
)
public class ProductController {

    private final AddProductService addProductService;
    private final UpdateProductStockService updateProductStockService;
    private final DeleteProductService deleteProductService;

    public ProductController(AddProductService addProductService, UpdateProductStockService updateProductStockService,
                             DeleteProductService deleteProductService) {
        this.addProductService = addProductService;
        this.updateProductStockService = updateProductStockService;
        this.deleteProductService = deleteProductService;
    }

    @PostMapping
    public Mono<ResponseEntity<ProductResponse>> add(
            @PathVariable("franchiseId") UUID franchiseId,
            @PathVariable("branchId") UUID branchId,
            @Valid @RequestBody CreateProductRequest request
    ) {
        return addProductService.add(franchiseId, branchId, request.name(), request.stock())
                .map(product -> {
                    ProductResponse response = ProductResponse.from(branchId, product);

                    URI location = URI.create(
                            "/api/v1/franchises/" + franchiseId
                                    + "/branches/" + branchId
                                    + "/products/" + product.id()
                    );

                    return ResponseEntity.created(location).body(response);
                });
    }

    @PatchMapping("/{productId}/stock")
    public Mono<ResponseEntity<ProductResponse>> updateStock(
            @PathVariable("franchiseId") UUID franchiseId,
            @PathVariable("branchId") UUID branchId,
            @PathVariable("productId") UUID productId,
            @Valid @RequestBody UpdateStockRequest request
    ) {
        return updateProductStockService.update(franchiseId, branchId, productId, request.stock())
                .map(product -> ResponseEntity.ok(ProductResponse.from(branchId, product)));
    }

    @DeleteMapping("/{productId}")
    public Mono<ResponseEntity<Void>> delete(
            @PathVariable("franchiseId") UUID franchiseId,
            @PathVariable("branchId") UUID branchId,
            @PathVariable("productId") UUID productId
    ) {
        return deleteProductService.delete(franchiseId, branchId, productId).thenReturn(
                ResponseEntity.noContent().<Void>build()
        );
    }
}