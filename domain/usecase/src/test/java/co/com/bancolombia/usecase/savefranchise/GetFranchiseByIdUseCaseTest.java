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
class GetFranchiseByIdUseCaseTest {

    @InjectMocks
    GetFranchiseByIdUseCase useCase;

    @Mock
    FranchiseRepository repository;

    @Test
    void shouldFindFranchiseByIdSuccessfully() {
        Franchise franchise = new Franchise("id-1", Optional.of("Test Franchise"));
        when(repository.findFranchiseById("id-1")).thenReturn(Mono.just(franchise));

        StepVerifier.create(useCase.run("id-1"))
                .expectNextMatches(found -> found.id().equals("id-1")
                        && found.name().equals(Optional.of("Test Franchise")))
                .verifyComplete();

        verify(repository).findFranchiseById("id-1");
    }

    @Test
    void shouldReturnEmptyWhenFranchiseNotFound() {
        when(repository.findFranchiseById("unknown-id")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.run("unknown-id"))
                .verifyComplete();

        verify(repository).findFranchiseById("unknown-id");
    }

    @Test
    void shouldPropagateErrorFromRepository() {
        RuntimeException error = new RuntimeException("DB connection failed");
        when(repository.findFranchiseById("id-1")).thenReturn(Mono.error(error));

        StepVerifier.create(useCase.run("id-1"))
                .expectErrorMatches(ex -> ex instanceof RuntimeException
                        && ex.getMessage().equals("DB connection failed"))
                .verify();
    }
}
