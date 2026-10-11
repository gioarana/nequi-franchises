package com.nequi.franchises.application.service;

import com.nequi.franchises.application.port.out.FranchiseRepository;
import com.nequi.franchises.domain.model.Franchise;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

public class CreateFranchiseService {

    private final FranchiseRepository repository;

    public CreateFranchiseService(FranchiseRepository repository) {
        this.repository = repository;
    }

    public Mono<Franchise> create(String name) {
        return Mono.defer(() -> {
            Franchise franchise = new Franchise(
                    UUID.randomUUID(),
                    name,
                    List.of()
            );

            return repository.create(franchise);
        });
    }
}