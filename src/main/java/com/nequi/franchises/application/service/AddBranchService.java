package com.nequi.franchises.application.service;

import com.nequi.franchises.application.port.out.FranchiseRepository;
import com.nequi.franchises.domain.exception.ResourceNotFoundException;
import com.nequi.franchises.domain.model.Branch;
import com.nequi.franchises.domain.model.Franchise;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AddBranchService {

    private final FranchiseRepository repository;

    public AddBranchService(FranchiseRepository repository) {
        this.repository = repository;
    }

    public Mono<Branch> add(UUID franchiseId, String name) {
        return Mono.defer(() -> {
            Branch branch = new Branch(
                    UUID.randomUUID(),
                    name,
                    List.of()
            );

            return repository.update(franchiseId, current -> {
                        List<Branch> branches =
                                new ArrayList<>(current.branches());

                        branches.add(branch);

                        return new Franchise(
                                current.id(),
                                current.name(),
                                branches
                        );
                    })
                    .switchIfEmpty(Mono.error(
                            new ResourceNotFoundException(
                                    "Franquicia no encontrada: " + franchiseId
                            )
                    ))
                    .map(updated -> branch);
        });
    }
}