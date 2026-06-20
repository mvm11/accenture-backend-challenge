package co.com.bancolombia.usecase.branch;

import co.com.bancolombia.model.branch.Branch;
import co.com.bancolombia.model.branch.gateways.BranchRepository;
import co.com.bancolombia.model.product.gateways.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindBranchByIdUseCaseTest {

    @InjectMocks
    FindBranchByIdUseCase useCase;

    @Mock
    BranchRepository branchRepository;

    @Mock
    ProductRepository productRepository;

    @Test
    @DisplayName("should return branch with its products when found")
    void shouldFindBranchByIdSuccessfully() {
        Branch branch = new Branch("id-1", Optional.of("Test Branch"), null);
        when(branchRepository.findBranchById("franchise-1", "id-1")).thenReturn(Mono.just(branch));
        when(productRepository.findProductsByBranchId("id-1")).thenReturn(Flux.empty());

        StepVerifier.create(useCase.run("franchise-1", "id-1"))
                .expectNextMatches(found -> found.id().equals("id-1")
                        && found.name().equals(Optional.of("Test Branch"))
                        && found.products().isEmpty())
                .verifyComplete();

        verify(branchRepository).findBranchById("franchise-1", "id-1");
        verify(productRepository).findProductsByBranchId("id-1");
    }

    @Test
    @DisplayName("should return empty stream when branch is not found")
    void shouldReturnEmptyWhenBranchNotFound() {
        when(branchRepository.findBranchById("franchise-1", "unknown-id")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.run("franchise-1", "unknown-id"))
                .verifyComplete();

        verify(branchRepository).findBranchById("franchise-1", "unknown-id");
    }

    @Test
    @DisplayName("should propagate error from repository")
    void shouldPropagateErrorFromRepository() {
        RuntimeException error = new RuntimeException("DB connection failed");
        when(branchRepository.findBranchById("franchise-1", "id-1")).thenReturn(Mono.error(error));

        StepVerifier.create(useCase.run("franchise-1", "id-1"))
                .expectErrorMatches(ex -> ex instanceof RuntimeException
                        && ex.getMessage().equals("DB connection failed"))
                .verify();
    }
}
