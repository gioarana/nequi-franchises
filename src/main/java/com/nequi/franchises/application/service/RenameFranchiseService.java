package com.nequi.franchises.application.service;

import com.nequi.franchises.application.port.out.FranchiseRepository;
import com.nequi.franchises.domain.exception.ResourceNotFoundException;
import com.nequi.franchises.domain.model.Franchise;
import reactor.core.publisher.Mono;

import java.util.UUID;

public class RenameFranchiseService {

    private final FranchiseRepository repository;

    public RenameFranchiseService(FranchiseRepository repository) {
        this.repository = repository;
    }

    public Mono<Franchise> rename(UUID franchiseId, String name) {
        return Mono.defer(() ->
                repository.update(franchiseId, current -> new Franchise(current.id(), name, current.branches()))
                        .switchIfEmpty(Mono.error(new ResourceNotFoundException("Franquicia no encontrada: " + franchiseId))));
    }
}