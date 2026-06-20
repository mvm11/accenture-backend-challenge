package co.com.bancolombia.model.franchise;

import co.com.bancolombia.model.franchise.exceptions.InvalidFranchiseException;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class FranchiseTest {

    @Test
    void shouldCreateFranchiseSuccessfully() {
        Franchise franchise = new Franchise("id-1", Optional.of("Franchise Name"), null);

        assertEquals("id-1", franchise.id());
        assertEquals(Optional.of("Franchise Name"), franchise.name());
    }

    @Test
    void shouldThrowWhenNameIsEmpty() {
        InvalidFranchiseException ex = assertThrows(InvalidFranchiseException.class,
                () -> new Franchise("id-1", Optional.of(""), null));

        assertEquals("Franchise name must not be empty", ex.getMessage());
    }

    @Test
    void shouldThrowWhenNameIsBlank() {
        InvalidFranchiseException ex = assertThrows(InvalidFranchiseException.class,
                () -> new Franchise("id-1", Optional.of("   "), null));

        assertEquals("Franchise name must not be empty", ex.getMessage());
    }

    @Test
    void shouldThrowWhenNameIsAbsent() {
        assertThrows(InvalidFranchiseException.class,
                () -> new Franchise("id-1", Optional.empty(), null));
    }
}
