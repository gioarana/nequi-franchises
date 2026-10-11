package com.nequi.franchises.application.port.out;

import com.nequi.franchises.domain.model.Franchise;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FranchiseRepository {

    Mono<Franchise> create(Franchise franchise);

    Mono<Franchise> findById(UUID id);
}