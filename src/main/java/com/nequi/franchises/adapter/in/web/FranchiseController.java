package com.nequi.franchises.adapter.in.web;

import com.nequi.franchises.adapter.in.web.dto.BranchStockResponse;
import com.nequi.franchises.adapter.in.web.dto.CreateFranchiseRequest;
import com.nequi.franchises.adapter.in.web.dto.FranchiseResponse;
import com.nequi.franchises.adapter.in.web.dto.UpdateNameRequest;
import com.nequi.franchises.application.service.CreateFranchiseService;
import com.nequi.franchises.application.service.FindMaxStockProductsService;
import com.nequi.franchises.application.service.RenameFranchiseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/franchises")
public class FranchiseController {

    private final CreateFranchiseService service;
    private final FindMaxStockProductsService findMaxStockProductsService;
    private final RenameFranchiseService renameFranchiseService;

    public FranchiseController(CreateFranchiseService service, FindMaxStockProductsService findMaxStockProductsService,
                               RenameFranchiseService renameFranchiseService) {
        this.service = service;
        this.findMaxStockProductsService = findMaxStockProductsService;
        this.renameFranchiseService = renameFranchiseService;
    }

    @PostMapping
    public Mono<ResponseEntity<FranchiseResponse>> create(@Valid @RequestBody CreateFranchiseRequest request) {
        return service.create(request.name())
                .map(franchise -> {
                    FranchiseResponse response = FranchiseResponse.from(franchise);

                    URI location = URI.create("/api/v1/franchises/" + franchise.id());

                    return ResponseEntity.created(location).body(response);
                });
    }

    @GetMapping("/{franchiseId}/products/max-stock")
    public Mono<List<BranchStockResponse>> findMaxStockProducts(@PathVariable("franchiseId") UUID franchiseId) {
        return findMaxStockProductsService.find(franchiseId)
                .map(summaries -> summaries.stream()
                        .map(BranchStockResponse::from)
                        .toList());
    }

    @PatchMapping("/{franchiseId}/name")
    public Mono<ResponseEntity<FranchiseResponse>> rename(@PathVariable("franchiseId") UUID franchiseId,
                                                          @Valid @RequestBody UpdateNameRequest request) {
        return renameFranchiseService.rename(franchiseId, request.name())
                .map(franchise -> ResponseEntity.ok(FranchiseResponse.from(franchise)));
    }
}