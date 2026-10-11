package com.nequi.franchises.domain.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BranchTest {

    @Test
    void shouldCreateEmptyBranchAndTrimName() {
        UUID id = UUID.randomUUID();

        Branch branch = new Branch(id, "  Chapinero  ", List.of());

        assertEquals(id, branch.id());
        assertEquals("Chapinero", branch.name());
        assertTrue(branch.products().isEmpty());
    }

    @Test
    void shouldRejectMissingId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Branch(null, "Chapinero", List.of())
        );
    }

    @Test
    void shouldRejectMissingOrBlankName() {
        UUID id = UUID.randomUUID();

        assertThrows(
                IllegalArgumentException.class,
                () -> new Branch(id, null, List.of())
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new Branch(id, "   ", List.of())
        );
    }

    @Test
    void shouldValidateNameLengthAfterTrimming() {
        UUID id = UUID.randomUUID();
        String name = "a".repeat(100);

        Branch branch = new Branch(
                id, "  " + name + "  ", List.of()
        );

        assertEquals(name, branch.name());

        assertThrows(
                IllegalArgumentException.class,
                () -> new Branch(id, "a".repeat(101), List.of())
        );
    }

    @Test
    void shouldRejectMissingProducts() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Branch(UUID.randomUUID(), "Chapinero", null)
        );
    }

    @Test
    void shouldPreserveProductsWhenOriginalListChanges() {
        Product coffee = new Product(
                UUID.randomUUID(), "Café", 20
        );
        List<Product> original = new ArrayList<>();
        original.add(coffee);

        Branch branch = new Branch(
                UUID.randomUUID(), "Chapinero", original
        );
        original.clear();

        assertEquals(List.of(coffee), branch.products());
    }

    @Test
    void shouldPreventChangesThroughProductsAccessor() {
        Branch branch = new Branch(
                UUID.randomUUID(), "Chapinero", List.of()
        );
        Product coffee = new Product(
                UUID.randomUUID(), "Café", 20
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> branch.products().add(coffee)
        );
    }

    @Test
    void shouldRejectNullProductInsideList() {
        List<Product> products = new ArrayList<>();
        products.add(null);

        assertThrows(
                NullPointerException.class,
                () -> new Branch(
                        UUID.randomUUID(), "Chapinero", products
                )
        );
    }
}