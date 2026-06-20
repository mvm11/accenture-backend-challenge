package co.com.bancolombia.usecase.branch;

import co.com.bancolombia.model.branch.Branch;
import co.com.bancolombia.model.branch.gateways.BranchRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateBranchNameUseCaseTest {

    @InjectMocks
    UpdateBranchNameUseCase useCase;

    @Mock
    BranchRepository repository;

    @Test
    @DisplayName("should update branch name successfully")
    void shouldUpdateBranchNameSuccessfully() {
        Branch updatedBranch = new Branch("id-1", Optional.of("New Name"));
        when(repository.updateBranchName("franchise-1", "id-1", "New Name")).thenReturn(Mono.just(updatedBranch));

        StepVerifier.create(useCase.run("franchise-1", "id-1", "New Name"))
                .expectNextMatches(updated -> updated.id().equals("id-1")
                        && updated.name().equals(Optional.of("New Name")))
                .verifyComplete();

        verify(repository).updateBranchName("franchise-1", "id-1", "New Name");
    }

    @Test
    @DisplayName("should return empty stream when branch is not found")
    void shouldReturnEmptyWhenBranchNotFound() {
        when(repository.updateBranchName("franchise-1", "unknown-id", "New Name")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.run("franchise-1", "unknown-id", "New Name"))
                .verifyComplete();

        verify(repository).updateBranchName("franchise-1", "unknown-id", "New Name");
    }

    @Test
    @DisplayName("should propagate error from repository")
    void shouldPropagateErrorFromRepository() {
        RuntimeException error = new RuntimeException("DB connection failed");
        when(repository.updateBranchName("franchise-1", "id-1", "New Name")).thenReturn(Mono.error(error));

        StepVerifier.create(useCase.run("franchise-1", "id-1", "New Name"))
                .expectErrorMatches(ex -> ex instanceof RuntimeException
                        && ex.getMessage().equals("DB connection failed"))
                .verify();
    }
}
