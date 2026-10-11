package com.nequi.franchises.application.port.out;

import com.nequi.franchises.domain.model.Franchise;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.function.UnaryOperator;

public interface FranchiseRepository {

    Mono<Franchise> create(Franchise franchise);

    Mono<Franchise> findById(UUID id);

    Mono<Franchise> update(
            UUID id,
            UnaryOperator<Franchise> change
    );
}