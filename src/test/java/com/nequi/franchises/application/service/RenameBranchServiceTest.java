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

class RenameBranchServiceTest {

    private FranchiseRepository repository;
    private RenameBranchService service;

    private UUID franchiseId;
    private UUID branchId;
    private Branch targetBranch;
    private Branch otherBranch;
    private Franchise current;

    @BeforeEach
    void setUp() {
        repository = mock(FranchiseRepository.class);
        service = new RenameBranchService(repository);

        franchiseId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        targetBranch = new Branch(branchId, "Chapinero", List.of(new Product(UUID.randomUUID(), "Café", 35)));

        otherBranch = new Branch(UUID.randomUUID(), "Laureles", List.of(new Product(UUID.randomUUID(), "Chocolate", 10)));

        current = new Franchise(franchiseId, "Café Colombiano", List.of(targetBranch, otherBranch));
    }

    @Test
    void shouldRenameOnlyTargetBranchAndPreserveInventory() {
        useCurrentFranchise();

        Branch expected = new Branch(branchId, "Chapinero Central", targetBranch.products());

        StepVerifier.create(service.rename(franchiseId, branchId, "  Chapinero Central  "))
                .assertNext(branch -> assertEquals(expected, branch))
                .verifyComplete();

        verify(repository).update(eq(franchiseId), argThat(change -> change.apply(current).equals(new Franchise(
                current.id(), current.name(), List.of(expected, otherBranch)))));

        assertEquals("Chapinero", targetBranch.name());
        assertEquals(List.of(targetBranch, otherBranch), current.branches());
    }

    @Test
    void shouldRejectBlankName() {
        useCurrentFranchise();

        StepVerifier.create(service.rename(franchiseId, branchId, "   "))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void shouldRejectNameLongerThan100Characters() {
        useCurrentFranchise();

        StepVerifier.create(service.rename(franchiseId, branchId, "a".repeat(101)))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void shouldFailWhenFranchiseDoesNotExist() {
        when(repository.update(eq(franchiseId), any())).thenReturn(Mono.empty());

        StepVerifier.create(service.rename(franchiseId, branchId, "Chapinero Central"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void shouldFailWhenBranchDoesNotBelongToFranchise() {
        useCurrentFranchise();

        StepVerifier.create(service.rename(franchiseId, UUID.randomUUID(), "Chapinero Central"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void shouldPropagatePersistenceFailure() {
        RuntimeException failure = new RuntimeException("Base de datos no disponible");

        when(repository.update(eq(franchiseId), any())).thenReturn(Mono.error(failure));

        StepVerifier.create(service.rename(franchiseId, branchId, "Chapinero Central"))
                .expectErrorMatches(error -> error == failure)
                .verify();
    }

    private void useCurrentFranchise() {
        when(repository.update(eq(franchiseId), any())).thenAnswer(invocation -> {
                    UnaryOperator<Franchise> change = invocation.getArgument(1);

                    return Mono.fromSupplier(() -> change.apply(current));
                });
    }
}