package co.com.bancolombia.usecase.savefranchise;

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
class SaveFranchiseUseCaseTest {

    @InjectMocks
    SaveFranchiseUseCase useCase;

    @Mock
    FranchiseRepository repository;

    @Test
    void shouldSaveFranchiseSuccessfully() {
        Franchise franchise = new Franchise("id-1", Optional.of("Test Franchise"));
        when(repository.saveFranchise(franchise)).thenReturn(Mono.just(franchise));

        StepVerifier.create(useCase.save(franchise))
                .expectNextMatches(saved -> saved.id().equals("id-1")
                        && saved.name().equals(Optional.of("Test Franchise")))
                .verifyComplete();

        verify(repository).saveFranchise(franchise);
    }

    @Test
    void shouldPropagateErrorFromRepository() {
        Franchise franchise = new Franchise("id-1", Optional.of("Test Franchise"));
        RuntimeException error = new RuntimeException("DB connection failed");
        when(repository.saveFranchise(franchise)).thenReturn(Mono.error(error));

        StepVerifier.create(useCase.save(franchise))
                .expectErrorMatches(ex -> ex instanceof RuntimeException
                        && ex.getMessage().equals("DB connection failed"))
                .verify();
    }
}
