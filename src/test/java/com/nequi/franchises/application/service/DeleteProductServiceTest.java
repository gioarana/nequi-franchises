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

class DeleteProductServiceTest {

    private FranchiseRepository repository;
    private DeleteProductService service;

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
        service = new DeleteProductService(repository);

        franchiseId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        coffee = new Product(UUID.randomUUID(), "Café", 35);
        chocolate = new Product(UUID.randomUUID(), "Chocolate", 10);

        targetBranch = new Branch(
                branchId,
                "Chapinero",
                List.of(coffee, chocolate)
        );

        otherBranch = new Branch(
                UUID.randomUUID(),
                "Laureles",
                List.of(new Product(UUID.randomUUID(), "Té", 15))
        );

        current = new Franchise(
                franchiseId,
                "Café Andino",
                List.of(targetBranch, otherBranch)
        );
    }

    @Test
    void shouldDeleteOnlyTargetProduct() {
        useCurrentFranchise();

        StepVerifier.create(
                        service.delete(franchiseId, branchId, coffee.id())
                )
                .verifyComplete();

        verify(repository).update(
                eq(franchiseId),
                argThat(change -> {
                    Franchise updated = change.apply(current);

                    Branch expectedBranch = new Branch(
                            targetBranch.id(),
                            targetBranch.name(),
                            List.of(chocolate)
                    );

                    return updated.equals(new Franchise(
                            current.id(),
                            current.name(),
                            List.of(expectedBranch, otherBranch)
                    ));
                })
        );

        assertEquals(
                List.of(coffee, chocolate),
                targetBranch.products()
        );
    }

    @Test
    void shouldAllowDeletingLastProduct() {
        current = new Franchise(
                franchiseId,
                "Café Andino",
                List.of(new Branch(
                        branchId, "Chapinero", List.of(coffee)
                ))
        );

        useCurrentFranchise();

        StepVerifier.create(
                        service.delete(franchiseId, branchId, coffee.id())
                )
                .verifyComplete();

        verify(repository).update(
                eq(franchiseId),
                argThat(change ->
                        change.apply(current)
                                .branches().getFirst()
                                .products().isEmpty()
                )
        );
    }

    @Test
    void shouldFailWhenFranchiseDoesNotExist() {
        when(repository.update(eq(franchiseId), any()))
                .thenReturn(Mono.empty());

        StepVerifier.create(
                        service.delete(franchiseId, branchId, coffee.id())
                )
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void shouldFailWhenBranchDoesNotBelongToFranchise() {
        useCurrentFranchise();

        StepVerifier.create(
                        service.delete(
                                franchiseId,
                                UUID.randomUUID(),
                                coffee.id()
                        )
                )
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void shouldFailWhenProductDoesNotBelongToBranch() {
        useCurrentFranchise();

        UUID otherProductId =
                otherBranch.products().getFirst().id();

        StepVerifier.create(
                        service.delete(franchiseId, branchId, otherProductId)
                )
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void shouldPropagatePersistenceFailure() {
        RuntimeException failure =
                new RuntimeException("Base de datos no disponible");

        when(repository.update(eq(franchiseId), any()))
                .thenReturn(Mono.error(failure));

        StepVerifier.create(
                        service.delete(franchiseId, branchId, coffee.id())
                )
                .expectErrorMatches(error -> error == failure)
                .verify();
    }

    private void useCurrentFranchise() {
        when(repository.update(eq(franchiseId), any()))
                .thenAnswer(invocation -> {
                    UnaryOperator<Franchise> change =
                            invocation.getArgument(1);

                    return Mono.fromSupplier(
                            () -> change.apply(current)
                    );
                });
    }
}