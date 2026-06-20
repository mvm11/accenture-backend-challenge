package co.com.bancolombia.usecase.franchise;

import co.com.bancolombia.model.branch.Branch;
import co.com.bancolombia.model.branch.gateways.BranchRepository;
import co.com.bancolombia.model.franchise.Franchise;
import co.com.bancolombia.model.franchise.gateways.FranchiseRepository;
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

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetFranchiseByIdUseCaseTest {

    private static final String FRANCHISE_ID = "id-1";
    private static final String UNKNOWN_ID = "unknown-id";
    private static final String FRANCHISE_NAME = "Test Franchise";
    private static final String DB_ERROR = "DB connection failed";

    @InjectMocks
    GetFranchiseByIdUseCase useCase;

    @Mock
    FranchiseRepository franchiseRepository;

    @Mock
    BranchRepository branchRepository;

    @Mock
    ProductRepository productRepository;

    @Test
    @DisplayName("should return franchise with its branches and products when found")
    void shouldFindFranchiseWithBranchesAndProductsByIdSuccessfully() {
        Franchise franchise = new Franchise(FRANCHISE_ID, Optional.of(FRANCHISE_NAME), null);
        Branch branch1 = new Branch("b-1", Optional.of("Downtown"), null);
        Branch branch2 = new Branch("b-2", Optional.of("Uptown"), null);
        when(franchiseRepository.findFranchiseById(FRANCHISE_ID)).thenReturn(Mono.just(franchise));
        when(branchRepository.findBranchesByFranchiseId(FRANCHISE_ID)).thenReturn(Flux.just(branch1, branch2));
        when(productRepository.findProductsByBranchId("b-1")).thenReturn(Flux.empty());
        when(productRepository.findProductsByBranchId("b-2")).thenReturn(Flux.empty());

        StepVerifier.create(useCase.run(FRANCHISE_ID))
                .expectNextMatches(found ->
                        found.id().equals(FRANCHISE_ID)
                        && found.name().equals(Optional.of(FRANCHISE_NAME))
                        && found.branches().size() == 2
                        && found.branches().get(0).id().equals("b-1")
                        && found.branches().get(0).products().isEmpty()
                        && found.branches().get(1).id().equals("b-2")
                        && found.branches().get(1).products().isEmpty())
                .verifyComplete();

        verify(franchiseRepository).findFranchiseById(FRANCHISE_ID);
        verify(branchRepository).findBranchesByFranchiseId(FRANCHISE_ID);
    }

    @Test
    @DisplayName("should return franchise with empty branch list when it has no branches")
    void shouldReturnFranchiseWithEmptyBranchesWhenNoneExist() {
        Franchise franchise = new Franchise(FRANCHISE_ID, Optional.of(FRANCHISE_NAME), null);
        when(franchiseRepository.findFranchiseById(FRANCHISE_ID)).thenReturn(Mono.just(franchise));
        when(branchRepository.findBranchesByFranchiseId(FRANCHISE_ID)).thenReturn(Flux.empty());

        StepVerifier.create(useCase.run(FRANCHISE_ID))
                .expectNextMatches(found ->
                        found.id().equals(FRANCHISE_ID)
                        && found.branches().equals(List.of()))
                .verifyComplete();
    }

    @Test
    @DisplayName("should return empty stream when franchise is not found")
    void shouldReturnEmptyWhenFranchiseNotFound() {
        when(franchiseRepository.findFranchiseById(UNKNOWN_ID)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.run(UNKNOWN_ID))
                .verifyComplete();

        verify(franchiseRepository).findFranchiseById(UNKNOWN_ID);
    }

    @Test
    @DisplayName("should propagate error from franchise repository")
    void shouldPropagateErrorFromFranchiseRepository() {
        RuntimeException error = new RuntimeException(DB_ERROR);
        when(franchiseRepository.findFranchiseById(FRANCHISE_ID)).thenReturn(Mono.error(error));

        StepVerifier.create(useCase.run(FRANCHISE_ID))
                .expectErrorMatches(ex -> ex instanceof RuntimeException
                        && ex.getMessage().equals(DB_ERROR))
                .verify();
    }

    @Test
    @DisplayName("should propagate error from branch repository")
    void shouldPropagateErrorFromBranchRepository() {
        Franchise franchise = new Franchise(FRANCHISE_ID, Optional.of(FRANCHISE_NAME), null);
        RuntimeException error = new RuntimeException("Branch DB failed");
        when(franchiseRepository.findFranchiseById(FRANCHISE_ID)).thenReturn(Mono.just(franchise));
        when(branchRepository.findBranchesByFranchiseId(FRANCHISE_ID)).thenReturn(Flux.error(error));

        StepVerifier.create(useCase.run(FRANCHISE_ID))
                .expectErrorMatches(ex -> ex instanceof RuntimeException
                        && ex.getMessage().equals("Branch DB failed"))
                .verify();
    }
}
