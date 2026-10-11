package com.nequi.franchises.application.service;

import com.nequi.franchises.application.port.out.FranchiseRepository;
import com.nequi.franchises.domain.exception.ResourceNotFoundException;
import com.nequi.franchises.domain.model.Branch;
import com.nequi.franchises.domain.model.Franchise;
import com.nequi.franchises.domain.model.Product;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public class DeleteProductService {

    private final FranchiseRepository repository;

    public DeleteProductService(FranchiseRepository repository) {
        this.repository = repository;
    }

    public Mono<Void> delete(UUID franchiseId, UUID branchId, UUID productId) {
        return Mono.defer(() ->
                repository.update(franchiseId, current -> {
                    Branch targetBranch = current.branches().stream()
                            .filter(branch -> branch.id().equals(branchId))
                            .findFirst()
                            .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada: " + branchId));

                    boolean productExists = targetBranch.products().stream().anyMatch(product -> product.id().equals(productId));

                    if (!productExists) {
                        throw new ResourceNotFoundException("Producto no encontrado: " + productId);
                    }

                    List<Product> products = targetBranch.products().stream().filter(
                            product -> !product.id().equals(productId)
                    ).toList();

                    Branch updatedBranch = new Branch(targetBranch.id(), targetBranch.name(), products);

                    List<Branch> branches = current.branches().stream().map(
                            branch -> branch.id().equals(branchId) ? updatedBranch : branch
                            ).toList();

                    return new Franchise(current.id(), current.name(), branches);
                })
                    .switchIfEmpty(Mono.error(new ResourceNotFoundException("Franquicia no encontrada: " + franchiseId)))
                    .then()
        );
    }
}