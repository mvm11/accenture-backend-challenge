package co.com.bancolombia.usecase.franchise;

import co.com.bancolombia.model.franchise.Franchise;
import co.com.bancolombia.model.franchise.gateways.FranchiseRepository;
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
class UpdateFranchiseNameUseCaseTest {

    @InjectMocks
    UpdateFranchiseNameUseCase useCase;

    @Mock
    FranchiseRepository repository;

    @Test
    void shouldUpdateFranchiseNameSuccessfully() {
        Franchise updatedFranchise = new Franchise("id-1", Optional.of("New Name"), null);
        when(repository.updateFranchiseName("id-1", "New Name")).thenReturn(Mono.just(updatedFranchise));

        StepVerifier.create(useCase.run("id-1", "New Name"))
                .expectNextMatches(updated -> updated.id().equals("id-1")
                        && updated.name().equals(Optional.of("New Name")))
                .verifyComplete();

        verify(repository).updateFranchiseName("id-1", "New Name");
    }

    @Test
    void shouldReturnEmptyWhenFranchiseNotFound() {
        when(repository.updateFranchiseName("unknown-id", "New Name")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.run("unknown-id", "New Name"))
                .verifyComplete();

        verify(repository).updateFranchiseName("unknown-id", "New Name");
    }

    @Test
    void shouldPropagateErrorFromRepository() {
        RuntimeException error = new RuntimeException("DB connection failed");
        when(repository.updateFranchiseName("id-1", "New Name")).thenReturn(Mono.error(error));

        StepVerifier.create(useCase.run("id-1", "New Name"))
                .expectErrorMatches(ex -> ex instanceof RuntimeException
                        && ex.getMessage().equals("DB connection failed"))
                .verify();
    }
}
