package com.nequi.franchises.application.service;

import com.nequi.franchises.application.model.BranchStockSummary;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FindMaxStockProductsServiceTest {

    private FranchiseRepository repository;
    private FindMaxStockProductsService service;
    private UUID franchiseId;

    @BeforeEach
    void setUp() {
        repository = mock(FranchiseRepository.class);
        service = new FindMaxStockProductsService(repository);
        franchiseId = UUID.randomUUID();
    }

    @Test
    void shouldReturnMaximumPerBranchInBranchIdOrder() {
        UUID firstBranchId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID secondBranchId = UUID.fromString("00000000-0000-0000-0000-000000000002");

        Product coffee = new Product(UUID.randomUUID(), "Café", 20);
        Product chocolate = new Product(UUID.randomUUID(), "Chocolate", 35);
        Product tea = new Product(UUID.randomUUID(), "Té", 50);

        Branch first = new Branch(firstBranchId, "Chapinero", List.of(coffee, chocolate));
        Branch second = new Branch(secondBranchId, "Laureles", List.of(tea));

        useBranches(List.of(second, first));

        StepVerifier.create(service.find(franchiseId))
                .assertNext(result -> assertEquals(
                        List.of(new BranchStockSummary(firstBranchId, "Chapinero", chocolate.id(), "Chocolate", 35),
                                new BranchStockSummary(secondBranchId, "Laureles", tea.id(), "Té", 50)
                        ), result))
                .verifyComplete();

        verify(repository, times(1)).findById(franchiseId);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void shouldChooseLexicographicallySmallestIdOnTie() {
        Product smaller = new Product(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Café", 20);
        Product larger = new Product(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Chocolate", 20);

        useBranches(List.of(new Branch(UUID.randomUUID(), "Chapinero", List.of(larger, smaller))));

        StepVerifier.create(service.find(franchiseId))
                .assertNext(result -> {
                    assertEquals(1, result.size());
                    assertEquals(smaller.id(), result.getFirst().productId());
                })
                .verifyComplete();
    }

    @Test
    void shouldIncludeZeroStockAndOmitEmptyBranches() {
        Product coffee = new Product(UUID.randomUUID(), "Café", 0);
        UUID populatedBranchId = UUID.randomUUID();

        useBranches(List.of(
                new Branch(UUID.randomUUID(), "Vacía", List.of()),
                new Branch(populatedBranchId, "Chapinero", List.of(coffee))
        ));

        StepVerifier.create(service.find(franchiseId))
                .assertNext(result -> assertEquals(
                        List.of(new BranchStockSummary(populatedBranchId, "Chapinero", coffee.id(), "Café", 0)), result))
                .verifyComplete();
    }

    @Test
    void shouldReturnEmptyListWhenAllBranchesAreEmpty() {
        useBranches(List.of(new Branch(UUID.randomUUID(), "Chapinero", List.of())));

        StepVerifier.create(service.find(franchiseId)).assertNext(result -> assertTrue(result.isEmpty())).verifyComplete();
    }

    @Test
    void shouldReturnEmptyListWhenFranchiseHasNoBranches() {
        useBranches(List.of());

        StepVerifier.create(service.find(franchiseId))
                .assertNext(result -> assertTrue(result.isEmpty()))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenFranchiseDoesNotExist() {
        when(repository.findById(franchiseId)).thenReturn(Mono.empty());

        StepVerifier.create(service.find(franchiseId)).expectError(ResourceNotFoundException.class).verify();
    }

    @Test
    void shouldPropagatePersistenceFailure() {
        RuntimeException failure = new RuntimeException("Base de datos no disponible");

        when(repository.findById(franchiseId)).thenReturn(Mono.error(failure));

        StepVerifier.create(service.find(franchiseId)).expectErrorMatches(error -> error == failure).verify();
    }

    private void useBranches(List<Branch> branches) {
        when(repository.findById(franchiseId)).thenReturn(Mono.just(new Franchise(franchiseId, "Café Andino", branches)));
    }
}