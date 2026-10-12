package com.nequi.franchises.application.service;

import com.nequi.franchises.application.port.out.FranchiseRepository;
import com.nequi.franchises.domain.exception.ResourceNotFoundException;
import com.nequi.franchises.domain.model.Branch;
import com.nequi.franchises.domain.model.Franchise;
import com.nequi.franchises.domain.model.Product;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public class RenameProductService {

    private final FranchiseRepository repository;

    public RenameProductService(FranchiseRepository repository) {
        this.repository = repository;
    }

    public Mono<Product> rename(UUID franchiseId, UUID branchId, UUID productId, String name) {
        return Mono.defer(() -> repository.update(franchiseId, current -> {
            Branch targetBranch = current.branches().stream().filter(branch -> branch.id().equals(branchId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Sucursal no encontrada: " + branchId));

            Product targetProduct = targetBranch.products().stream().filter(product -> product.id().equals(productId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + productId));

            Product updatedProduct = new Product(targetProduct.id(), name, targetProduct.stock());

            List<Product> products = targetBranch.products().stream().map(
                    product -> product.id().equals(productId) ? updatedProduct : product)
                    .toList();

            Branch updatedBranch = new Branch(targetBranch.id(), targetBranch.name(), products);

            List<Branch> branches = current.branches().stream().map(
                    branch -> branch.id().equals(branchId) ? updatedBranch : branch)
                    .toList();

            return new Franchise(current.id(), current.name(), branches);
        })
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Franquicia no encontrada: " + franchiseId)))
                        .map(updated -> updated.branches().stream().filter(branch -> branch.id().equals(branchId))
                                .findFirst()
                                .orElseThrow()
                                .products().stream()
                                .filter(product -> product.id().equals(productId))
                                .findFirst()
                                .orElseThrow())
        );
    }
}