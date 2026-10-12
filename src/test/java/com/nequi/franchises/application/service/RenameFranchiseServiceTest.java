package com.nequi.franchises.application.service;

import com.nequi.franchises.application.port.out.FranchiseRepository;
import com.nequi.franchises.domain.exception.ResourceNotFoundException;
import com.nequi.franchises.domain.model.Branch;
import com.nequi.franchises.domain.model.Franchise;
import com.nequi.franchises.domain.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RenameFranchiseServiceTest {

    private FranchiseRepository repository;
    private RenameFranchiseService service;
    private UUID franchiseId;
    private Franchise current;

    @BeforeEach
    void setUp() {
        repository = mock(FranchiseRepository.class);
        service = new RenameFranchiseService(repository);
        franchiseId = UUID.randomUUID();

        Product coffee = new Product(UUID.randomUUID(), "Café", 50);

        Branch branch = new Branch(UUID.randomUUID(), "Laureles", List.of(coffee));

        current = new Franchise(franchiseId, "Café Andino", List.of(branch));
    }

    @Test
    void shouldRenameAndPreserveIdAndInventory() {
        useCurrentFranchise();

        StepVerifier.create(service.rename(franchiseId, "  Café Colombiano  "))
                .assertNext(updated -> {
                    assertEquals(current.id(), updated.id());
                    assertEquals("Café Colombiano", updated.name());
                    assertEquals(current.branches(), updated.branches());
                })
                .verifyComplete();

        verify(repository, times(1)).update(eq(franchiseId), any());

        assertEquals("Café Andino", current.name());
    }

    @Test
    void shouldRejectBlankName() {
        useCurrentFranchise();

        StepVerifier.create(service.rename(franchiseId, "   "))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void shouldRejectNameLongerThan100Characters() {
        useCurrentFranchise();

        StepVerifier.create(service.rename(franchiseId, "a".repeat(101)))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void shouldFailWhenFranchiseDoesNotExist() {
        when(repository.update(eq(franchiseId), any())).thenReturn(Mono.empty());

        StepVerifier.create(service.rename(franchiseId, "Café Colombiano"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void shouldPropagatePersistenceFailure() {
        RuntimeException failure = new RuntimeException("Base de datos no disponible");

        when(repository.update(eq(franchiseId), any())).thenReturn(Mono.error(failure));

        StepVerifier.create(service.rename(franchiseId, "Café Colombiano"))
                .expectErrorMatches(error -> error == failure)
                .verify();
    }

    private void useCurrentFranchise() {
        when(repository.update(eq(franchiseId), any()))
                .thenAnswer(invocation -> {
                    UnaryOperator<Franchise> change = invocation.getArgument(1);

                    return Mono.fromSupplier(() -> change.apply(current));
                });
    }
}