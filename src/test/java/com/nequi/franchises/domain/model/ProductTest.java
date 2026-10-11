package com.nequi.franchises.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void shouldCreateProductAndTrimName() {
        UUID id = UUID.randomUUID();

        Product product = new Product(id, "  Café  ", 20);

        assertEquals(id, product.id());
        assertEquals("Café", product.name());
        assertEquals(20, product.stock());
    }

    @Test
    void shouldAllowZeroStock() {
        Product product = new Product(
                UUID.randomUUID(), "Café", 0
        );

        assertEquals(0, product.stock());
    }

    @Test
    void shouldRejectNegativeStock() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Product(UUID.randomUUID(), "Café", -1)
        );
    }

    @Test
    void shouldRejectMissingId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Product(null, "Café", 20)
        );
    }

    @Test
    void shouldRejectMissingOrBlankName() {
        UUID id = UUID.randomUUID();

        assertThrows(
                IllegalArgumentException.class,
                () -> new Product(id, null, 20)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new Product(id, "   ", 20)
        );
    }

    @Test
    void shouldAllowNameWithExactly100Characters() {
        String name = "a".repeat(100);

        Product product = new Product(
                UUID.randomUUID(), name, 20
        );

        assertEquals(name, product.name());
    }

    @Test
    void shouldRejectNameLongerThan100Characters() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Product(
                        UUID.randomUUID(), "a".repeat(101), 20
                )
        );
    }
}