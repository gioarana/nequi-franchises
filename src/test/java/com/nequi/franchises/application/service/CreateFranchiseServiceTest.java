package com.nequi.franchises.application.service;

import com.nequi.franchises.application.port.out.FranchiseRepository;
import com.nequi.franchises.domain.model.Franchise;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CreateFranchiseServiceTest {

    private FranchiseRepository repository;
    private CreateFranchiseService service;

    @BeforeEach
    void setUp() {
        repository = mock(FranchiseRepository.class);
        service = new CreateFranchiseService(repository);
    }

    @Test
    void shouldCreateFranchiseWithGeneratedIdAndNoBranches() {
        when(repository.create(any(Franchise.class)))
                .thenAnswer(invocation -> {
                    Franchise franchise = invocation.getArgument(0);
                    return Mono.just(franchise);
                });

        StepVerifier.create(service.create("  Café Andino  "))
                .assertNext(franchise -> {
                    assertNotNull(franchise.id());
                    assertEquals("Café Andino", franchise.name());
                    assertTrue(franchise.branches().isEmpty());
                })
                .verifyComplete();

        verify(repository, times(1)).create(any(Franchise.class));
    }

    @Test
    void shouldDeliverInvalidNameAsReactiveError() {
        Mono<Franchise> result = assertDoesNotThrow(
                () -> service.create("   ")
        );

        StepVerifier.create(result)
                .expectError(IllegalArgumentException.class)
                .verify();

        verifyNoInteractions(repository);
    }

    @Test
    void shouldPropagateRepositoryError() {
        RuntimeException failure = new RuntimeException(
                "Database unavailable"
        );

        when(repository.create(any(Franchise.class)))
                .thenReturn(Mono.error(failure));

        StepVerifier.create(service.create("Café Andino"))
                .expectErrorMatches(error -> error == failure)
                .verify();

        verify(repository, times(1)).create(any(Franchise.class));
    }
}