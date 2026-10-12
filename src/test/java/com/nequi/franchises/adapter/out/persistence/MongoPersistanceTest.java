package com.nequi.franchises.adapter.out.persistence;

import com.nequi.franchises.adapter.out.persistence.repository.SpringDataFranchiseRepository;
import com.nequi.franchises.application.port.out.FranchiseRepository;
import com.nequi.franchises.domain.model.Branch;
import com.nequi.franchises.domain.model.Franchise;
import com.nequi.franchises.domain.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.annotation.DirtiesContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(properties = {
        "spring.mongodb.ssl.enabled=false"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class MongoPersistenceTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(20);

    @Container
    @ServiceConnection
    static final MongoDBContainer mongo =
            new MongoDBContainer("mongo:8.0");

    @Autowired
    private FranchiseRepository repository;

    @Autowired
    private SpringDataFranchiseRepository documentRepository;

    @Test
    void shouldPersistAndRecoverCompleteInventory() {
        Franchise original = sampleFranchise();

        StepVerifier.create(
                        repository.create(original)
                                .flatMap(saved ->
                                        repository.findById(saved.id())
                                )
                )
                .assertNext(recovered ->
                        assertEquals(original, recovered)
                )
                .expectComplete()
                .verify(TIMEOUT);
    }

    @Test
    void shouldUpdateAndIncrementVersionWithoutLosingInventory() {
        Franchise original = sampleFranchise();

        StepVerifier.create(
                        repository.create(original)
                                .then(documentRepository.findById(
                                        original.id().toString()
                                ))
                                .flatMap(before -> {
                                    Long previousVersion = before.getVersion();
                                    assertNotNull(previousVersion);

                                    return repository.update(
                                                    original.id(),
                                                    current -> new Franchise(
                                                            current.id(),
                                                            "Nombre actualizado",
                                                            current.branches()
                                                    )
                                            )
                                            .then(Mono.zip(
                                                    repository.findById(
                                                            original.id()
                                                    ),
                                                    documentRepository.findById(
                                                            original.id().toString()
                                                    )
                                            ))
                                            .doOnNext(result -> {
                                                Franchise recovered =
                                                        result.getT1();

                                                assertEquals(
                                                        original.id(),
                                                        recovered.id()
                                                );
                                                assertEquals(
                                                        "Nombre actualizado",
                                                        recovered.name()
                                                );
                                                assertEquals(
                                                        original.branches(),
                                                        recovered.branches()
                                                );
                                                assertEquals(
                                                        Long.valueOf(
                                                                previousVersion + 1
                                                        ),
                                                        result.getT2().getVersion()
                                                );
                                            });
                                })
                )
                .expectNextCount(1)
                .expectComplete()
                .verify(TIMEOUT);
    }

    @Test
    void shouldRejectWriteUsingStaleVersion() {
        Franchise original = sampleFranchise();

        StepVerifier.create(
                        repository.create(original)
                                .then(Mono.zip(
                                        documentRepository.findById(
                                                original.id().toString()
                                        ),
                                        documentRepository.findById(
                                                original.id().toString()
                                        )
                                ))
                                .flatMap(copies -> {
                                    var first = copies.getT1();
                                    var stale = copies.getT2();

                                    assertEquals(
                                            first.getVersion(),
                                            stale.getVersion()
                                    );

                                    first.updateContent(
                                            "Primer cambio",
                                            first.getBranches()
                                    );

                                    stale.updateContent(
                                            "Cambio desactualizado",
                                            stale.getBranches()
                                    );

                                    return documentRepository.save(first)
                                            .then(documentRepository.save(stale));
                                })
                )
                .expectError(OptimisticLockingFailureException.class)
                .verify(TIMEOUT);

        StepVerifier.create(repository.findById(original.id()))
                .assertNext(recovered -> {
                    assertEquals("Primer cambio", recovered.name());
                    assertEquals(
                            original.branches(),
                            recovered.branches()
                    );
                })
                .expectComplete()
                .verify(TIMEOUT);
    }

    private Franchise sampleFranchise() {
        Product product = new Product(
                UUID.randomUUID(), "Café", 20
        );

        Branch branch = new Branch(
                UUID.randomUUID(), "Chapinero", List.of(product)
        );

        return new Franchise(
                UUID.randomUUID(), "Café Andino", List.of(branch)
        );
    }
}