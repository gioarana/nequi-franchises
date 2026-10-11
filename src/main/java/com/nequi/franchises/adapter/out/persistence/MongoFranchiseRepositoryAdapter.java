package com.nequi.franchises.adapter.out.persistence;

import com.nequi.franchises.adapter.out.persistence.document.FranchiseDocument;
import com.nequi.franchises.adapter.out.persistence.repository.SpringDataFranchiseRepository;
import com.nequi.franchises.application.port.out.FranchiseRepository;
import com.nequi.franchises.domain.model.Franchise;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public class MongoFranchiseRepositoryAdapter
        implements FranchiseRepository {

    private final SpringDataFranchiseRepository repository;

    public MongoFranchiseRepositoryAdapter(
            SpringDataFranchiseRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public Mono<Franchise> create(Franchise franchise) {
        return Mono.defer(() -> {
            FranchiseDocument document = new FranchiseDocument(
                    franchise.id().toString(),
                    franchise.name(),
                    franchise.branches()
            );

            return repository.insert(document)
                    .map(this::toDomain);
        });
    }

    @Override
    public Mono<Franchise> findById(UUID id) {
        return Mono.defer(() ->
                repository.findById(id.toString())
                        .map(this::toDomain)
        );
    }

    private Franchise toDomain(FranchiseDocument document) {
        return new Franchise(
                UUID.fromString(document.getId()),
                document.getName(),
                document.getBranches()
        );
    }
}