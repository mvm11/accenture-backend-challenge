package co.com.bancolombia.model.branch;

import co.com.bancolombia.model.branch.exceptions.InvalidBranchException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BranchTest {

    @Test
    @DisplayName("should create branch successfully when name is valid")
    void shouldCreateBranchSuccessfully() {
        Branch branch = new Branch("id-1", Optional.of("My Branch"));

        assertEquals("id-1", branch.id());
        assertEquals(Optional.of("My Branch"), branch.name());
    }

    @Test
    @DisplayName("should throw when name is empty")
    void shouldThrowWhenNameIsEmpty() {
        InvalidBranchException ex = assertThrows(InvalidBranchException.class,
                () -> new Branch("id-1", Optional.of("")));

        assertEquals("Branch name must not be empty", ex.getMessage());
    }

    @Test
    @DisplayName("should throw when name is blank")
    void shouldThrowWhenNameIsBlank() {
        InvalidBranchException ex = assertThrows(InvalidBranchException.class,
                () -> new Branch("id-1", Optional.of("   ")));

        assertEquals("Branch name must not be empty", ex.getMessage());
    }

    @Test
    @DisplayName("should throw when name is absent")
    void shouldThrowWhenNameIsAbsent() {
        assertThrows(InvalidBranchException.class,
                () -> new Branch("id-1", Optional.empty()));
    }
}
