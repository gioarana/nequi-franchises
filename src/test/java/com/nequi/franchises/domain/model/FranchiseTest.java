package com.nequi.franchises.domain.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FranchiseTest {

    @Test
    void shouldCreateEmptyFranchiseAndTrimName() {
        UUID id = UUID.randomUUID();

        Franchise franchise = new Franchise(
                id, "  Café Andino  ", List.of()
        );

        assertEquals(id, franchise.id());
        assertEquals("Café Andino", franchise.name());
        assertTrue(franchise.branches().isEmpty());
    }

    @Test
    void shouldRejectMissingId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Franchise(null, "Café Andino", List.of())
        );
    }

    @Test
    void shouldRejectMissingOrBlankName() {
        UUID id = UUID.randomUUID();

        assertThrows(
                IllegalArgumentException.class,
                () -> new Franchise(id, null, List.of())
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new Franchise(id, "   ", List.of())
        );
    }

    @Test
    void shouldValidateNameLengthAfterTrimming() {
        UUID id = UUID.randomUUID();
        String name = "a".repeat(100);

        Franchise franchise = new Franchise(
                id, "  " + name + "  ", List.of()
        );

        assertEquals(name, franchise.name());

        assertThrows(
                IllegalArgumentException.class,
                () -> new Franchise(id, "a".repeat(101), List.of())
        );
    }

    @Test
    void shouldRejectMissingBranches() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Franchise(
                        UUID.randomUUID(), "Café Andino", null
                )
        );
    }

    @Test
    void shouldPreserveBranchesWhenOriginalListChanges() {
        Branch branch = new Branch(
                UUID.randomUUID(), "Chapinero", List.of()
        );
        List<Branch> original = new ArrayList<>();
        original.add(branch);

        Franchise franchise = new Franchise(
                UUID.randomUUID(), "Café Andino", original
        );
        original.clear();

        assertEquals(List.of(branch), franchise.branches());
    }

    @Test
    void shouldPreventChangesThroughBranchesAccessor() {
        Franchise franchise = new Franchise(
                UUID.randomUUID(), "Café Andino", List.of()
        );
        Branch branch = new Branch(
                UUID.randomUUID(), "Chapinero", List.of()
        );

        assertThrows(
                UnsupportedOperationException.class,
                () -> franchise.branches().add(branch)
        );
    }

    @Test
    void shouldRejectNullBranchInsideList() {
        List<Branch> branches = new ArrayList<>();
        branches.add(null);

        assertThrows(
                NullPointerException.class,
                () -> new Franchise(
                        UUID.randomUUID(), "Café Andino", branches
                )
        );
    }
}