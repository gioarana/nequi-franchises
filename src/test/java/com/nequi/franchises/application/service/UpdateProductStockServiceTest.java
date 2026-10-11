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

class UpdateProductStockServiceTest {

    private FranchiseRepository repository;
    private UpdateProductStockService service;

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
        service = new UpdateProductStockService(repository);

        franchiseId = UUID.randomUUID();
        branchId = UUID.randomUUID();

        coffee = new Product(UUID.randomUUID(), "Café", 20);
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
    void shouldReplaceStockAndPreserveOtherData() {
        useCurrentFranchise();

        StepVerifier.create(
                        service.update(
                                franchiseId, branchId, coffee.id(), 35
                        )
                )
                .assertNext(product ->
                        assertEquals(
                                new Product(coffee.id(), coffee.name(), 35),
                                product
                        )
                )
                .verifyComplete();

        verify(repository).update(
                eq(franchiseId),
                argThat(change -> {
                    Franchise updated = change.apply(current);

                    Branch expectedBranch = new Branch(
                            targetBranch.id(),
                            targetBranch.name(),
                            List.of(
                                    new Product(
                                            coffee.id(), coffee.name(), 35
                                    ),
                                    chocolate
                            )
                    );

                    return updated.equals(new Franchise(
                            current.id(),
                            current.name(),
                            List.of(expectedBranch, otherBranch)
                    ));
                })
        );

        assertEquals(20, coffee.stock());
        assertEquals(
                List.of(coffee, chocolate),
                targetBranch.products()
        );
    }

    @Test
    void shouldAllowZeroStock() {
        useCurrentFranchise();

        StepVerifier.create(
                        service.update(
                                franchiseId, branchId, coffee.id(), 0
                        )
                )
                .assertNext(product -> assertEquals(0, product.stock()))
                .verifyComplete();
    }

    @Test
    void shouldRejectNegativeStockBeforeCallingRepository() {
        StepVerifier.create(
                        service.update(
                                franchiseId, branchId, coffee.id(), -1
                        )
                )
                .expectError(IllegalArgumentException.class)
                .verify();

        verifyNoInteractions(repository);
    }

    @Test
    void shouldFailWhenFranchiseDoesNotExist() {
        when(repository.update(eq(franchiseId), any()))
                .thenReturn(Mono.empty());

        StepVerifier.create(
                        service.update(
                                franchiseId, branchId, coffee.id(), 35
                        )
                )
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void shouldFailWhenBranchDoesNotBelongToFranchise() {
        useCurrentFranchise();

        StepVerifier.create(
                        service.update(
                                franchiseId,
                                UUID.randomUUID(),
                                coffee.id(),
                                35
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
                        service.update(
                                franchiseId, branchId, otherProductId, 35
                        )
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
                        service.update(
                                franchiseId, branchId, coffee.id(), 35
                        )
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