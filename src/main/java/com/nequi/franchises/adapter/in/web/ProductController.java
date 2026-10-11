package com.nequi.franchises.adapter.in.web;

import com.nequi.franchises.adapter.in.web.dto.CreateProductRequest;
import com.nequi.franchises.adapter.in.web.dto.ProductResponse;
import com.nequi.franchises.application.service.AddProductService;
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

    public ProductController(AddProductService addProductService) {
        this.addProductService = addProductService;
    }

    @PostMapping
    public Mono<ResponseEntity<ProductResponse>> add(
            @PathVariable("franchiseId") UUID franchiseId,
            @PathVariable("branchId") UUID branchId,
            @Valid @RequestBody CreateProductRequest request
    ) {
        return addProductService.add(
                        franchiseId,
                        branchId,
                        request.name(),
                        request.stock()
                )
                .map(product -> {
                    ProductResponse response =
                            ProductResponse.from(branchId, product);

                    URI location = URI.create(
                            "/api/v1/franchises/" + franchiseId
                                    + "/branches/" + branchId
                                    + "/products/" + product.id()
                    );

                    return ResponseEntity.created(location)
                            .body(response);
                });
    }
}