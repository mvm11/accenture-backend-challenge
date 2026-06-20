package co.com.bancolombia.usecase.franchise;

import co.com.bancolombia.model.branch.Branch;
import co.com.bancolombia.model.branch.TopStockProduct;
import co.com.bancolombia.model.branch.gateways.BranchRepository;
import co.com.bancolombia.model.franchise.Franchise;
import co.com.bancolombia.model.franchise.exceptions.FranchiseNotFoundException;
import co.com.bancolombia.model.franchise.gateways.FranchiseRepository;
import co.com.bancolombia.model.product.Product;
import co.com.bancolombia.model.product.gateways.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigInteger;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTopStockProductPerBranchUseCaseTest {

    private static final String FRANCHISE_ID = "franchise-1";
    private static final String BRANCH_A_ID = "branch-a";
    private static final String BRANCH_B_ID = "branch-b";

    @InjectMocks
    GetTopStockProductPerBranchUseCase useCase;

    @Mock
    FranchiseRepository franchiseRepository;

    @Mock
    BranchRepository branchRepository;

    @Mock
    ProductRepository productRepository;

    private final Franchise franchise = new Franchise(FRANCHISE_ID, Optional.of("My Franchise"), null);
    private final Branch branchA = new Branch(BRANCH_A_ID, Optional.of("Branch A"), null);
    private final Branch branchB = new Branch(BRANCH_B_ID, Optional.of("Branch B"), null);

    @Nested
    @DisplayName("happy path — franchise with branches and products")
    class HappyPath {

        @Test
        @DisplayName("should return the highest-stock product for each branch")
        void shouldReturnTopStockPerBranch() {
            Product low  = new Product("p-1", "Low Stock",  BigInteger.valueOf(10));
            Product high = new Product("p-2", "High Stock", BigInteger.valueOf(99));
            Product only = new Product("p-3", "Only One",   BigInteger.valueOf(50));

            when(franchiseRepository.findFranchiseById(FRANCHISE_ID)).thenReturn(Mono.just(franchise));
            when(branchRepository.findBranchesByFranchiseId(FRANCHISE_ID)).thenReturn(Flux.just(branchA, branchB));
            when(productRepository.findProductsByBranchId(BRANCH_A_ID)).thenReturn(Flux.just(low, high));
            when(productRepository.findProductsByBranchId(BRANCH_B_ID)).thenReturn(Flux.just(only));

            StepVerifier.create(useCase.run(FRANCHISE_ID).sort(
                            (a, b) -> a.branchId().compareTo(b.branchId())))
                    .expectNextMatches(r ->
                            r.branchId().equals(BRANCH_A_ID)
                            && r.branchName().equals("Branch A")
                            && r.productId().equals("p-2")
                            && r.productName().equals("High Stock")
                            && r.stock().equals(BigInteger.valueOf(99)))
                    .expectNextMatches(r ->
                            r.branchId().equals(BRANCH_B_ID)
                            && r.productName().equals("Only One")
                            && r.stock().equals(BigInteger.valueOf(50)))
                    .verifyComplete();
        }

        @Test
        @DisplayName("should pick the product with the highest stock when stocks are tied")
        void shouldPickFirstWhenStocksAreTied() {
            Product first  = new Product("p-1", "First",  BigInteger.valueOf(30));
            Product second = new Product("p-2", "Second", BigInteger.valueOf(30));

            when(franchiseRepository.findFranchiseById(FRANCHISE_ID)).thenReturn(Mono.just(franchise));
            when(branchRepository.findBranchesByFranchiseId(FRANCHISE_ID)).thenReturn(Flux.just(branchA));
            when(productRepository.findProductsByBranchId(BRANCH_A_ID)).thenReturn(Flux.just(first, second));

            StepVerifier.create(useCase.run(FRANCHISE_ID))
                    .expectNextMatches(r -> r.stock().equals(BigInteger.valueOf(30)))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("franchise not found → 404 signal")
    class FranchiseNotFound {

        @Test
        @DisplayName("should emit FranchiseNotFoundException when franchise does not exist")
        void shouldEmitErrorWhenFranchiseNotFound() {
            when(franchiseRepository.findFranchiseById("unknown")).thenReturn(Mono.empty());

            StepVerifier.create(useCase.run("unknown"))
                    .expectErrorMatches(ex ->
                            ex instanceof FranchiseNotFoundException
                            && ex.getMessage().contains("unknown"))
                    .verify();
        }
    }

    @Nested
    @DisplayName("branch with no products → excluded from result")
    class BranchWithNoProducts {

        @Test
        @DisplayName("should exclude branches that have no products")
        void shouldExcludeBranchesWithNoProducts() {
            Product product = new Product("p-1", "Some Product", BigInteger.valueOf(20));

            when(franchiseRepository.findFranchiseById(FRANCHISE_ID)).thenReturn(Mono.just(franchise));
            when(branchRepository.findBranchesByFranchiseId(FRANCHISE_ID)).thenReturn(Flux.just(branchA, branchB));
            when(productRepository.findProductsByBranchId(BRANCH_A_ID)).thenReturn(Flux.just(product));
            when(productRepository.findProductsByBranchId(BRANCH_B_ID)).thenReturn(Flux.empty());

            StepVerifier.create(useCase.run(FRANCHISE_ID))
                    .expectNextMatches(r -> r.branchId().equals(BRANCH_A_ID))
                    .verifyComplete();
        }

        @Test
        @DisplayName("should return empty result when franchise has no branches")
        void shouldReturnEmptyWhenNoBranches() {
            when(franchiseRepository.findFranchiseById(FRANCHISE_ID)).thenReturn(Mono.just(franchise));
            when(branchRepository.findBranchesByFranchiseId(FRANCHISE_ID)).thenReturn(Flux.empty());

            StepVerifier.create(useCase.run(FRANCHISE_ID))
                    .verifyComplete();
        }

        @Test
        @DisplayName("should return empty result when all branches have no products")
        void shouldReturnEmptyWhenAllBranchesHaveNoProducts() {
            when(franchiseRepository.findFranchiseById(FRANCHISE_ID)).thenReturn(Mono.just(franchise));
            when(branchRepository.findBranchesByFranchiseId(FRANCHISE_ID)).thenReturn(Flux.just(branchA, branchB));
            when(productRepository.findProductsByBranchId(BRANCH_A_ID)).thenReturn(Flux.empty());
            when(productRepository.findProductsByBranchId(BRANCH_B_ID)).thenReturn(Flux.empty());

            StepVerifier.create(useCase.run(FRANCHISE_ID))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("error propagation")
    class ErrorPropagation {

        @Test
        @DisplayName("should propagate error from franchise repository")
        void shouldPropagateErrorFromFranchiseRepository() {
            when(franchiseRepository.findFranchiseById(FRANCHISE_ID))
                    .thenReturn(Mono.error(new RuntimeException("DB down")));

            StepVerifier.create(useCase.run(FRANCHISE_ID))
                    .expectErrorMatches(ex -> ex.getMessage().equals("DB down"))
                    .verify();
        }

        @Test
        @DisplayName("should propagate error from branch repository")
        void shouldPropagateErrorFromBranchRepository() {
            when(franchiseRepository.findFranchiseById(FRANCHISE_ID)).thenReturn(Mono.just(franchise));
            when(branchRepository.findBranchesByFranchiseId(FRANCHISE_ID))
                    .thenReturn(Flux.error(new RuntimeException("Branch DB down")));

            StepVerifier.create(useCase.run(FRANCHISE_ID))
                    .expectErrorMatches(ex -> ex.getMessage().equals("Branch DB down"))
                    .verify();
        }

        @Test
        @DisplayName("should propagate error from product repository")
        void shouldPropagateErrorFromProductRepository() {
            when(franchiseRepository.findFranchiseById(FRANCHISE_ID)).thenReturn(Mono.just(franchise));
            when(branchRepository.findBranchesByFranchiseId(FRANCHISE_ID)).thenReturn(Flux.just(branchA));
            when(productRepository.findProductsByBranchId(BRANCH_A_ID))
                    .thenReturn(Flux.error(new RuntimeException("Product DB down")));

            StepVerifier.create(useCase.run(FRANCHISE_ID))
                    .expectErrorMatches(ex -> ex.getMessage().equals("Product DB down"))
                    .verify();
        }
    }
}
