package co.com.bancolombia.api.product;

import co.com.bancolombia.api.RouterRest;
import co.com.bancolombia.api.branch.BranchHandler;
import co.com.bancolombia.api.franchise.FranchiseHandler;
import co.com.bancolombia.model.product.Product;
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

import java.math.BigInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {RouterRest.class, FranchiseHandler.class, BranchHandler.class, ProductHandler.class})
class ProductHandlerTest {

    private static final String FRANCHISE_ID = "franchise-1";
    private static final String BRANCH_ID = "branch-1";
    private static final String PRODUCT_ID = "product-1";
    private static final String PRODUCT_NAME = "My Product";
    private static final String NEW_NAME = "Updated Product";
    private static final BigInteger STOCK = BigInteger.TEN;
    private static final BigInteger NEW_STOCK = BigInteger.valueOf(99);
    private static final String UNEXPECTED_FAILURE = "unexpected failure";

    private static final Product PRODUCT = new Product(PRODUCT_ID, PRODUCT_NAME, STOCK);

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

    private String productsPath() {
        return franchisesPath + "/" + FRANCHISE_ID + "/branches/" + BRANCH_ID + "/products";
    }

    private String productPath() {
        return productsPath() + "/" + PRODUCT_ID;
    }

    @Nested
    @DisplayName("createProduct – happy path")
    class CreateProductHappyPath {

        @Test
        @DisplayName("should return 201 when product is created successfully")
        void shouldReturn201OnSuccess() {
            when(saveProductUseCase.run(eq(BRANCH_ID), any())).thenReturn(Mono.just(PRODUCT));

            webTestClient.post()
                    .uri(productsPath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {"name": "My Product", "stock": 10}
                            """)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody()
                    .jsonPath("$.id").isEqualTo(PRODUCT_ID)
                    .jsonPath("$.name").isEqualTo(PRODUCT_NAME);
        }
    }

    @Nested
    @DisplayName("createProduct – error propagation")
    class CreateProductErrorPropagation {

        @Test
        @DisplayName("should return 5xx when the use case throws")
        void shouldReturn5xxWhenUseCaseFails() {
            when(saveProductUseCase.run(eq(BRANCH_ID), any()))
                    .thenReturn(Mono.error(new RuntimeException(UNEXPECTED_FAILURE)));

            webTestClient.post()
                    .uri(productsPath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {"name": "My Product", "stock": 10}
                            """)
                    .exchange()
                    .expectStatus().is5xxServerError();
        }

        @Test
        @DisplayName("should return 5xx when product name is null (domain validation fails)")
        void shouldReturn5xxWhenNameIsNull() {
            webTestClient.post()
                    .uri(productsPath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {"name": null, "stock": 10}
                            """)
                    .exchange()
                    .expectStatus().is5xxServerError();
        }
    }

    @Nested
    @DisplayName("getProductById – happy path")
    class GetProductByIdHappyPath {

        @Test
        @DisplayName("should return 200 with product body when found")
        void shouldReturn200WhenProductFound() {
            when(getProductByIdUseCase.run(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID)).thenReturn(Mono.just(PRODUCT));

            webTestClient.get()
                    .uri(productPath())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.id").isEqualTo(PRODUCT_ID)
                    .jsonPath("$.name").isEqualTo(PRODUCT_NAME);
        }
    }

    @Nested
    @DisplayName("getProductById – not found")
    class GetProductByIdNotFound {

        @Test
        @DisplayName("should return 404 when product is not found")
        void shouldReturn404WhenNotFound() {
            when(getProductByIdUseCase.run(FRANCHISE_ID, BRANCH_ID, "unknown-id")).thenReturn(Mono.empty());

            webTestClient.get()
                    .uri(productsPath() + "/unknown-id")
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    @Nested
    @DisplayName("getProductById – error propagation")
    class GetProductByIdErrorPropagation {

        @Test
        @DisplayName("should return 5xx when the use case throws")
        void shouldReturn5xxOnError() {
            when(getProductByIdUseCase.run(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID))
                    .thenReturn(Mono.error(new RuntimeException(UNEXPECTED_FAILURE)));

            webTestClient.get()
                    .uri(productPath())
                    .exchange()
                    .expectStatus().is5xxServerError();
        }
    }

    @Nested
    @DisplayName("updateProductName – happy path")
    class UpdateProductNameHappyPath {

        @Test
        @DisplayName("should return 200 with updated product body")
        void shouldReturn200WhenNameUpdated() {
            Product updated = new Product(PRODUCT_ID, NEW_NAME, STOCK);
            when(updateProductUseCase.updateName(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID, NEW_NAME))
                    .thenReturn(Mono.just(updated));

            webTestClient.patch()
                    .uri(productPath() + "/name")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {"name": "Updated Product"}
                            """)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.id").isEqualTo(PRODUCT_ID)
                    .jsonPath("$.name").isEqualTo(NEW_NAME);
        }
    }

    @Nested
    @DisplayName("updateProductName – not found")
    class UpdateProductNameNotFound {

        @Test
        @DisplayName("should return 404 when product is not found")
        void shouldReturn404WhenNotFound() {
            when(updateProductUseCase.updateName(FRANCHISE_ID, BRANCH_ID, "unknown-id", NEW_NAME))
                    .thenReturn(Mono.empty());

            webTestClient.patch()
                    .uri(productsPath() + "/unknown-id/name")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {"name": "Updated Product"}
                            """)
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    @Nested
    @DisplayName("updateProductName – error propagation")
    class UpdateProductNameErrorPropagation {

        @Test
        @DisplayName("should return 5xx when the use case throws")
        void shouldReturn5xxOnError() {
            when(updateProductUseCase.updateName(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID, NEW_NAME))
                    .thenReturn(Mono.error(new RuntimeException(UNEXPECTED_FAILURE)));

            webTestClient.patch()
                    .uri(productPath() + "/name")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {"name": "Updated Product"}
                            """)
                    .exchange()
                    .expectStatus().is5xxServerError();
        }
    }

    @Nested
    @DisplayName("updateProductStock – happy path")
    class UpdateProductStockHappyPath {

        @Test
        @DisplayName("should return 200 with updated product body")
        void shouldReturn200WhenStockUpdated() {
            Product updated = new Product(PRODUCT_ID, PRODUCT_NAME, NEW_STOCK);
            when(updateProductUseCase.updateStock(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID, NEW_STOCK))
                    .thenReturn(Mono.just(updated));

            webTestClient.patch()
                    .uri(productPath() + "/stock")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {"stock": 99}
                            """)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.id").isEqualTo(PRODUCT_ID)
                    .jsonPath("$.stock").isEqualTo(99);
        }
    }

    @Nested
    @DisplayName("updateProductStock – not found")
    class UpdateProductStockNotFound {

        @Test
        @DisplayName("should return 404 when product is not found")
        void shouldReturn404WhenNotFound() {
            when(updateProductUseCase.updateStock(FRANCHISE_ID, BRANCH_ID, "unknown-id", NEW_STOCK))
                    .thenReturn(Mono.empty());

            webTestClient.patch()
                    .uri(productsPath() + "/unknown-id/stock")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {"stock": 99}
                            """)
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    @Nested
    @DisplayName("updateProductStock – error propagation")
    class UpdateProductStockErrorPropagation {

        @Test
        @DisplayName("should return 5xx when the use case throws")
        void shouldReturn5xxOnError() {
            when(updateProductUseCase.updateStock(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID, NEW_STOCK))
                    .thenReturn(Mono.error(new RuntimeException(UNEXPECTED_FAILURE)));

            webTestClient.patch()
                    .uri(productPath() + "/stock")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {"stock": 99}
                            """)
                    .exchange()
                    .expectStatus().is5xxServerError();
        }
    }

    @Nested
    @DisplayName("deleteProduct – happy path")
    class DeleteProductHappyPath {

        @Test
        @DisplayName("should return 200 with deleted product body")
        void shouldReturn200WhenDeleted() {
            when(deleteProductUseCase.run(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID)).thenReturn(Mono.just(PRODUCT));

            webTestClient.delete()
                    .uri(productPath())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.id").isEqualTo(PRODUCT_ID);
        }
    }

    @Nested
    @DisplayName("deleteProduct – not found")
    class DeleteProductNotFound {

        @Test
        @DisplayName("should return 404 when product is not found")
        void shouldReturn404WhenNotFound() {
            when(deleteProductUseCase.run(FRANCHISE_ID, BRANCH_ID, "unknown-id")).thenReturn(Mono.empty());

            webTestClient.delete()
                    .uri(productsPath() + "/unknown-id")
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    @Nested
    @DisplayName("deleteProduct – error propagation")
    class DeleteProductErrorPropagation {

        @Test
        @DisplayName("should return 5xx when the use case throws")
        void shouldReturn5xxOnError() {
            when(deleteProductUseCase.run(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID))
                    .thenReturn(Mono.error(new RuntimeException(UNEXPECTED_FAILURE)));

            webTestClient.delete()
                    .uri(productPath())
                    .exchange()
                    .expectStatus().is5xxServerError();
        }
    }
}
