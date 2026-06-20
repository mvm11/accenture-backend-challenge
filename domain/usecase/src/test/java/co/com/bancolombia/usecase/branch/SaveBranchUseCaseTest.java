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
class SaveBranchUseCaseTest {

    @InjectMocks
    SaveBranchUseCase useCase;

    @Mock
    BranchRepository repository;

    @Test
    @DisplayName("should delegate to repository and return saved branch")
    void shouldSaveBranchSuccessfully() {
        Branch branch = new Branch("id-1", Optional.of("Test Branch"), null);
        when(repository.saveBranch("franchise-1", branch)).thenReturn(Mono.just(branch));

        StepVerifier.create(useCase.run("franchise-1", branch))
                .expectNextMatches(saved -> saved.id().equals("id-1")
                        && saved.name().equals(Optional.of("Test Branch")))
                .verifyComplete();

        verify(repository).saveBranch("franchise-1", branch);
    }

    @Test
    @DisplayName("should propagate error from repository")
    void shouldPropagateErrorFromRepository() {
        Branch branch = new Branch("id-1", Optional.of("Test Branch"), null);
        RuntimeException error = new RuntimeException("DB connection failed");
        when(repository.saveBranch("franchise-1", branch)).thenReturn(Mono.error(error));

        StepVerifier.create(useCase.run("franchise-1", branch))
                .expectErrorMatches(ex -> ex instanceof RuntimeException
                        && ex.getMessage().equals("DB connection failed"))
                .verify();
    }
}
