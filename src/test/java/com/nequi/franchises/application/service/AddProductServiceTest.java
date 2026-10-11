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

class AddProductServiceTest {

    private FranchiseRepository repository;
    private AddProductService service;

    @BeforeEach
    void setUp() {
        repository = mock(FranchiseRepository.class);
        service = new AddProductService(repository);
    }

    @Test
    void shouldAddProductOnlyToTargetBranch() {
        UUID franchiseId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();

        Product existing = new Product(
                UUID.randomUUID(), "Chocolate", 10
        );

        Branch target = new Branch(
                branchId, "Chapinero", List.of(existing)
        );

        Branch other = new Branch(
                UUID.randomUUID(), "Laureles", List.of()
        );

        Franchise current = new Franchise(
                franchiseId, "Café Andino", List.of(target, other)
        );

        when(repository.update(eq(franchiseId), any()))
                .thenAnswer(invocation -> {
                    UnaryOperator<Franchise> change =
                            invocation.getArgument(1);

                    return Mono.just(change.apply(current));
                });

        StepVerifier.create(
                        service.add(franchiseId, branchId, "  Café  ", 20)
                )
                .assertNext(product -> {
                    assertNotNull(product.id());
                    assertEquals("Café", product.name());
                    assertEquals(20, product.stock());

                    verify(repository).update(
                            eq(franchiseId),
                            argThat(change -> {
                                Franchise updated = change.apply(current);
                                Branch updatedBranch =
                                        updated.branches().getFirst();

                                return updated.id().equals(current.id())
                                        && updated.name().equals(current.name())
                                        && updatedBranch.id().equals(target.id())
                                        && updatedBranch.name().equals(target.name())
                                        && updatedBranch.products().equals(
                                        List.of(existing, product)
                                )
                                        && updated.branches().getLast().equals(other);
                            })
                    );
                })
                .verifyComplete();

        assertEquals(List.of(target, other), current.branches());
        assertEquals(List.of(existing), target.products());
    }

    @Test
    void shouldFailWhenBranchDoesNotBelongToFranchise() {
        UUID franchiseId = UUID.randomUUID();

        Franchise current = new Franchise(
                franchiseId,
                "Café Andino",
                List.of(new Branch(
                        UUID.randomUUID(), "Chapinero", List.of()
                ))
        );

        when(repository.update(eq(franchiseId), any()))
                .thenAnswer(invocation -> {
                    UnaryOperator<Franchise> change =
                            invocation.getArgument(1);

                    return Mono.fromSupplier(() -> change.apply(current));
                });

        StepVerifier.create(service.add(
                        franchiseId,
                        UUID.randomUUID(),
                        "Café",
                        20
                ))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void shouldFailWhenFranchiseDoesNotExist() {
        UUID franchiseId = UUID.randomUUID();

        when(repository.update(eq(franchiseId), any()))
                .thenReturn(Mono.empty());

        StepVerifier.create(service.add(
                        franchiseId, UUID.randomUUID(), "Café", 20
                ))
                .expectError(ResourceNotFoundException.class)
                .verify();
    }

    @Test
    void shouldRejectNegativeStockBeforeCallingRepository() {
        StepVerifier.create(service.add(
                        UUID.randomUUID(),
                        UUID.randomUUID(),
                        "Café",
                        -1
                ))
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

        StepVerifier.create(service.add(
                        franchiseId, UUID.randomUUID(), "Café", 20
                ))
                .expectErrorMatches(error -> error == failure)
                .verify();
    }
}