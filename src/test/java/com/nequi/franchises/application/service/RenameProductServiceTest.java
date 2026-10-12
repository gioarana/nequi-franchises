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

class RenameProductServiceTest {

    private FranchiseRepository repository;
    private RenameProductService service;

    private UUID franchiseId;
    private UUID branchId;
    private Product coffee;
    private Product chocolate;
    private Branch targetBranch;
    private Branch otherBranch;
    private Franchise current;

    @BeforeEach
    void setUp() {
        repository = mock(FranchiseRepository.class);
        service = new RenameProductService(repository);

        franchiseId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        coffee = new Product(UUID.randomUUID(), "Café", 20);
        chocolate = new Product(UUID.randomUUID(), "Chocolate", 35);

        targetBranch = new Branch(branchId, "Chapinero Central", List.of(coffee, chocolate));

        otherBranch = new Branch(UUID.randomUUID(), "Laureles", List.of(new Product(UUID.randomUUID(), "Café", 50)));

        current = new Franchise(franchiseId, "Café Colombiano", List.of(targetBranch, otherBranch));
    }

    @Test
    void shouldRenameOnlyTargetProductAndPreserveStock() {
        useCurrentFranchise();

        Product expected = new Product(coffee.id(), "Café Premium", coffee.stock());

        StepVerifier.create(service.rename(franchiseId, branchId, coffee.id(), "  Café Premium  "))
                .assertNext(product -> assertEquals(expected, product))
                .verifyComplete();

        verify(repository).update(eq(franchiseId), argThat(
                change -> change.apply(current).equals(new Franchise(current.id(), current.name(),
                                List.of(new Branch(targetBranch.id(), targetBranch.name(), List.of(expected, chocolate)),
                                        otherBranch)))));

        assertEquals("Café", coffee.name());
        assertEquals(List.of(coffee, chocolate), targetBranch.products());
    }

    @Test
    void shouldRejectBlankName() {
        useCurrentFranchise();

        StepVerifier.create(service.rename(franchiseId, branchId, coffee.id(), "   "))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void shouldRejectNameLongerThan100Characters() {
        useCurrentFranchise();

        StepVerifier.create(service.rename(franchiseId, branchId, coffee.id(), "a".repeat(101)))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void shouldFailWhenFranchiseDoesNotExist() {
        when(repository.update(eq(franchiseId), any())).thenReturn(Mono.empty());

        StepVerifier.create(service.rename(franchiseId, branchId, coffee.id(), "Café Premium"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void shouldFailWhenBranchDoesNotBelongToFranchise() {
        useCurrentFranchise();

        StepVerifier.create(service.rename(franchiseId, UUID.randomUUID(), coffee.id(), "Café Premium"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void shouldFailWhenProductDoesNotBelongToBranch() {
        useCurrentFranchise();

        UUID otherProductId = otherBranch.products().getFirst().id();

        StepVerifier.create(service.rename(franchiseId, branchId, otherProductId, "Café Premium"))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void shouldPropagatePersistenceFailure() {
        RuntimeException failure = new RuntimeException("Base de datos no disponible");

        when(repository.update(eq(franchiseId), any())).thenReturn(Mono.error(failure));

        StepVerifier.create(service.rename(franchiseId, branchId, coffee.id(), "Café Premium"))
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