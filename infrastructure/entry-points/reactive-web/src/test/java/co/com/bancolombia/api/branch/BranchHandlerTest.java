package co.com.bancolombia.api.branch;

import co.com.bancolombia.api.RouterRest;
import co.com.bancolombia.api.franchise.FranchiseHandler;
import co.com.bancolombia.api.product.ProductHandler;
import co.com.bancolombia.model.branch.Branch;
import co.com.bancolombia.usecase.branch.FindBranchByIdUseCase;
import co.com.bancolombia.usecase.branch.SaveBranchUseCase;
import co.com.bancolombia.usecase.branch.UpdateBranchNameUseCase;
import co.com.bancolombia.usecase.franchise.GetFranchiseByIdUseCase;
import co.com.bancolombia.usecase.franchise.SaveFranchiseUseCase;
import co.com.bancolombia.usecase.franchise.UpdateFranchiseNameUseCase;
import co.com.bancolombia.usecase.product.DeleteProductUseCase;
import co.com.bancolombia.usecase.product.GetProductByIdUseCase;
import co.com.bancolombia.usecase.product.SaveProductUseCase;
import co.com.bancolombia.usecase.product.UpdateProductUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {RouterRest.class, FranchiseHandler.class, BranchHandler.class, ProductHandler.class})
class BranchHandlerTest {

    private static final String FRANCHISE_ID = "franchise-1";
    private static final String BRANCH_ID = "branch-1";
    private static final String NEW_NAME = "New Branch";
    private static final String UNEXPECTED_FAILURE = "unexpected failure";
    private static final String UPDATE_NAME_BODY = """
            {"name": "New Branch"}
            """;

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private SaveFranchiseUseCase saveFranchiseUseCase;

    @MockitoBean
    private GetFranchiseByIdUseCase getFranchiseByIdUseCase;

    @MockitoBean
    private UpdateFranchiseNameUseCase updateFranchiseNameUseCase;

    @MockitoBean
    private SaveBranchUseCase saveBranchUseCase;

    @MockitoBean
    private FindBranchByIdUseCase findBranchByIdUseCase;

    @MockitoBean
    private UpdateBranchNameUseCase updateBranchNameUseCase;

    @MockitoBean
    private SaveProductUseCase saveProductUseCase;

    @MockitoBean
    private GetProductByIdUseCase getProductByIdUseCase;

    @MockitoBean
    private UpdateProductUseCase updateProductUseCase;

    @MockitoBean
    private DeleteProductUseCase deleteProductUseCase;

    @Value("${api.paths.franchises}")
    private String franchisesPath;

    private String branchesPath() {
        return franchisesPath + "/" + FRANCHISE_ID + "/branches";
    }

    private String branchPath() {
        return branchesPath() + "/" + BRANCH_ID;
    }

    @Nested
    @DisplayName("createBranch – happy path")
    class CreateBranchHappyPath {

        @Test
        @DisplayName("should return 201 when branch is created successfully")
        void shouldReturn201OnSuccess() {
            Branch saved = new Branch(BRANCH_ID, Optional.of("My Branch"), null);
            when(saveBranchUseCase.run(eq(FRANCHISE_ID), any())).thenReturn(Mono.just(saved));

            webTestClient.post()
                    .uri(branchesPath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {"name": "My Branch"}
                            """)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody()
                    .jsonPath("$.id").isEqualTo(BRANCH_ID);
        }
    }

    @Nested
    @DisplayName("createBranch – error propagation")
    class CreateBranchErrorPropagation {

        @Test
        @DisplayName("should return 5xx when the use case throws")
        void shouldReturn5xxWhenUseCaseFails() {
            when(saveBranchUseCase.run(eq(FRANCHISE_ID), any()))
                    .thenReturn(Mono.error(new RuntimeException(UNEXPECTED_FAILURE)));

            webTestClient.post()
                    .uri(branchesPath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {"name": "My Branch"}
                            """)
                    .exchange()
                    .expectStatus().is5xxServerError();
        }

        @Test
        @DisplayName("should return 5xx when branch name is null (domain validation fails)")
        void shouldReturn5xxWhenNameIsNull() {
            webTestClient.post()
                    .uri(branchesPath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {"name": null}
                            """)
                    .exchange()
                    .expectStatus().is5xxServerError();
        }
    }

    @Nested
    @DisplayName("getBranchById – happy path")
    class GetBranchByIdHappyPath {

        @Test
        @DisplayName("should return 200 with branch body when found")
        void shouldReturn200WhenBranchFound() {
            Branch branch = new Branch(BRANCH_ID, Optional.of("My Branch"), null);
            when(findBranchByIdUseCase.run(FRANCHISE_ID, BRANCH_ID)).thenReturn(Mono.just(branch));

            webTestClient.get()
                    .uri(branchPath())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.id").isEqualTo(BRANCH_ID);
        }
    }

    @Nested
    @DisplayName("getBranchById – not found")
    class GetBranchByIdNotFound {

        @Test
        @DisplayName("should return 404 when branch is not found")
        void shouldReturn404WhenNotFound() {
            when(findBranchByIdUseCase.run(FRANCHISE_ID, "unknown-id")).thenReturn(Mono.empty());

            webTestClient.get()
                    .uri(franchisesPath + "/" + FRANCHISE_ID + "/branches/unknown-id")
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    @Nested
    @DisplayName("getBranchById – error propagation")
    class GetBranchByIdErrorPropagation {

        @Test
        @DisplayName("should return 5xx when the use case throws")
        void shouldReturn5xxOnError() {
            when(findBranchByIdUseCase.run(FRANCHISE_ID, BRANCH_ID))
                    .thenReturn(Mono.error(new RuntimeException(UNEXPECTED_FAILURE)));

            webTestClient.get()
                    .uri(branchPath())
                    .exchange()
                    .expectStatus().is5xxServerError();
        }
    }

    @Nested
    @DisplayName("updateBranchName – happy path")
    class UpdateBranchNameHappyPath {

        @Test
        @DisplayName("should return 200 with updated branch body")
        void shouldReturn200WhenUpdated() {
            Branch updated = new Branch(BRANCH_ID, Optional.of(NEW_NAME), null);
            when(updateBranchNameUseCase.run(FRANCHISE_ID, BRANCH_ID, NEW_NAME)).thenReturn(Mono.just(updated));

            webTestClient.patch()
                    .uri(branchPath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(UPDATE_NAME_BODY)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.id").isEqualTo(BRANCH_ID);
        }
    }

    @Nested
    @DisplayName("updateBranchName – not found")
    class UpdateBranchNameNotFound {

        @Test
        @DisplayName("should return 404 when branch is not found")
        void shouldReturn404WhenNotFound() {
            when(updateBranchNameUseCase.run(FRANCHISE_ID, "unknown-id", NEW_NAME)).thenReturn(Mono.empty());

            webTestClient.patch()
                    .uri(franchisesPath + "/" + FRANCHISE_ID + "/branches/unknown-id")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(UPDATE_NAME_BODY)
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    @Nested
    @DisplayName("updateBranchName – error propagation")
    class UpdateBranchNameErrorPropagation {

        @Test
        @DisplayName("should return 5xx when the use case throws")
        void shouldReturn5xxOnError() {
            when(updateBranchNameUseCase.run(FRANCHISE_ID, BRANCH_ID, NEW_NAME))
                    .thenReturn(Mono.error(new RuntimeException(UNEXPECTED_FAILURE)));

            webTestClient.patch()
                    .uri(branchPath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(UPDATE_NAME_BODY)
                    .exchange()
                    .expectStatus().is5xxServerError();
        }
    }
}
