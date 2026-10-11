package com.nequi.franchises.application.service;

import com.nequi.franchises.application.port.out.FranchiseRepository;
import com.nequi.franchises.domain.exception.ResourceNotFoundException;
import com.nequi.franchises.domain.model.Branch;
import com.nequi.franchises.domain.model.Franchise;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AddBranchServiceTest {

    private FranchiseRepository repository;
    private AddBranchService service;

    @BeforeEach
    void setUp() {
        repository = mock(FranchiseRepository.class);
        service = new AddBranchService(repository);
    }

    @Test
    void shouldAddBranchAndPreserveExistingData() {
        UUID franchiseId = UUID.randomUUID();

        Branch existing = new Branch(
                UUID.randomUUID(), "Laureles", List.of()
        );

        Franchise current = new Franchise(
                franchiseId, "Café Andino", List.of(existing)
        );

        when(repository.update(eq(franchiseId), any()))
                .thenAnswer(invocation -> {
                    UnaryOperator<Franchise> change =
                            invocation.getArgument(1);

                    return Mono.just(change.apply(current));
                });

        //ArgumentCaptor<Branch> created =
        //        ArgumentCaptor.forClass(Branch.class);

        StepVerifier.create(
                        service.add(franchiseId, "  Chapinero  ")
                )
                .assertNext(branch -> {
                    assertNotNull(branch.id());
                    assertEquals("Chapinero", branch.name());
                    assertTrue(branch.products().isEmpty());

                    //created.capture();
                })
                .verifyComplete();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<UnaryOperator<Franchise>> changeCaptor =
                ArgumentCaptor.forClass(UnaryOperator.class);

        verify(repository).update(
                eq(franchiseId), changeCaptor.capture()
        );

        Franchise updated = changeCaptor.getValue().apply(current);

        assertEquals(current.id(), updated.id());
        assertEquals(current.name(), updated.name());
        assertEquals(2, updated.branches().size());
        assertEquals(existing, updated.branches().getFirst());
        assertEquals("Chapinero", updated.branches().getLast().name());

        assertEquals(List.of(existing), current.branches());
    }

    @Test
    void shouldFailWhenFranchiseDoesNotExist() {
        UUID franchiseId = UUID.randomUUID();

        when(repository.update(eq(franchiseId), any()))
                .thenReturn(Mono.empty());

        StepVerifier.create(service.add(franchiseId, "Chapinero"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void shouldRejectInvalidNameBeforeCallingRepository() {
        StepVerifier.create(
                        service.add(UUID.randomUUID(), "   ")
                )
                .expectError(IllegalArgumentException.class)
                .verify();

        verifyNoInteractions(repository);
    }

    @Test
    void shouldPropagatePersistenceFailure() {
        UUID franchiseId = UUID.randomUUID();
        RuntimeException failure =
                new RuntimeException("Database unavailable");

        when(repository.update(eq(franchiseId), any()))
                .thenReturn(Mono.error(failure));

        StepVerifier.create(service.add(franchiseId, "Chapinero"))
                .expectErrorMatches(error -> error == failure)
                .verify();
    }
}