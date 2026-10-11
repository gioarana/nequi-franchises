package com.nequi.franchises.application.service;

import com.nequi.franchises.application.port.out.FranchiseRepository;
import com.nequi.franchises.domain.exception.ResourceNotFoundException;
import com.nequi.franchises.domain.model.Branch;
import com.nequi.franchises.domain.model.Franchise;
import com.nequi.franchises.domain.model.Product;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddProductService {

    private final FranchiseRepository repository;

    public AddProductService(FranchiseRepository repository) {
        this.repository = repository;
    }

    public Mono<Product> add(
            UUID franchiseId,
            UUID branchId,
            String name,
            int stock
    ) {
        return Mono.defer(() -> {
            Product product = new Product(
                    UUID.randomUUID(),
                    name,
                    stock
            );

            return repository.update(franchiseId, current -> {
                        Branch target = current.branches().stream()
                                .filter(branch ->
                                        branch.id().equals(branchId)
                                )
                                .findFirst()
                                .orElseThrow(() ->
                                        new ResourceNotFoundException(
                                                "Branch not found: " + branchId
                                        )
                                );

                        List<Product> products =
                                new ArrayList<>(target.products());

                        products.add(product);

                        Branch updatedBranch = new Branch(
                                target.id(),
                                target.name(),
                                products
                        );

                        List<Branch> branches =
                                current.branches().stream()
                                        .map(branch ->
                                                branch.id().equals(branchId)
                                                        ? updatedBranch
                                                        : branch
                                        )
                                        .toList();

                        return new Franchise(
                                current.id(),
                                current.name(),
                                branches
                        );
                    })
                    .switchIfEmpty(Mono.error(
                            new ResourceNotFoundException(
                                    "Franchise not found: " + franchiseId
                            )
                    ))
                    .map(updated -> product);
        });
    }
}