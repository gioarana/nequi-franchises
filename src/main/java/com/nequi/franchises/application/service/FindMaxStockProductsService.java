package com.nequi.franchises.application.service;

import com.nequi.franchises.application.model.BranchStockSummary;
import com.nequi.franchises.application.port.out.FranchiseRepository;
import com.nequi.franchises.domain.exception.ResourceNotFoundException;
import com.nequi.franchises.domain.model.Franchise;
import com.nequi.franchises.domain.model.Product;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class FindMaxStockProductsService {

    private static final Comparator<Product> PRODUCT_ORDER = Comparator.comparingInt(Product::stock)
            .thenComparing(product -> product.id().toString(), Comparator.reverseOrder());

    private final FranchiseRepository repository;

    public FindMaxStockProductsService(FranchiseRepository repository) {
        this.repository = repository;
    }

    public Mono<List<BranchStockSummary>> find(UUID franchiseId) {
        return Mono.defer(() -> repository.findById(franchiseId)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("Franquicia no encontrada: " + franchiseId)))
                .map(this::summarize));
    }

    private List<BranchStockSummary> summarize(Franchise franchise) {
        return franchise.branches().stream()
                .flatMap(branch -> branch.products().stream()
                        .max(PRODUCT_ORDER)
                        .stream()
                        .map(product -> new BranchStockSummary(branch.id(), branch.name(), product.id(), product.name(),
                                        product.stock())))
                .sorted(Comparator.comparing(summary -> summary.branchId().toString()))
                .toList();
    }
}