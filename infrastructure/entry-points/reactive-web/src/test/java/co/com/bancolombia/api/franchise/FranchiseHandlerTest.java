package co.com.bancolombia.api.franchise;

import co.com.bancolombia.api.RouterRest;
import co.com.bancolombia.model.franchise.Franchise;
import co.com.bancolombia.usecase.savefranchise.GetFranchiseByIdUseCase;
import co.com.bancolombia.usecase.savefranchise.SaveFranchiseUseCase;
import co.com.bancolombia.usecase.savefranchise.UpdateFranchiseNameUseCase;
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
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {RouterRest.class, FranchiseHandler.class})
class FranchiseHandlerTest {

    private static final String FRANCHISE_ID = "id-1";
    private static final String NEW_NAME = "New Name";
    private static final String UNEXPECTED_FAILURE = "unexpected failure";
    private static final String UPDATE_NAME_BODY = """
            {"name": "New Name"}
            """;

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private SaveFranchiseUseCase saveFranchiseUseCase;

    @MockitoBean
    private GetFranchiseByIdUseCase getFranchiseByIdUseCase;

    @MockitoBean
    private UpdateFranchiseNameUseCase updateFranchiseNameUseCase;

    @Value("${api.paths.franchises}")
    private String franchisesPath;

    @Nested
    @DisplayName("createFranchise – happy path")
    class HappyPath {

        @Test
        @DisplayName("should return 201 when franchise is created successfully")
        void shouldReturn201OnSuccess() {
            Franchise saved = new Franchise("uuid-1", Optional.of("My Franchise"));
            when(saveFranchiseUseCase.run(any())).thenReturn(Mono.just(saved));

            webTestClient.post()
                    .uri(franchisesPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {"name": "My Franchise"}
                            """)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody()
                    .jsonPath("$.id").isEqualTo("uuid-1");
        }

        @Test
        @DisplayName("should pass the request name to the use case")
        void shouldForwardNameToUseCase() {
            Franchise saved = new Franchise("uuid-2", Optional.of("Specific Name"));
            when(saveFranchiseUseCase.run(any(Franchise.class))).thenReturn(Mono.just(saved));

            webTestClient.post()
                    .uri(franchisesPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {"name": "Specific Name"}
                            """)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody()
                    .jsonPath("$.id").isEqualTo("uuid-2");
        }
    }

    @Nested
    @DisplayName("createFranchise – error propagation")
    class ErrorPropagation {

        @Test
        @DisplayName("should return 5xx when the use case throws")
        void shouldReturn5xxWhenUseCaseFails() {
            when(saveFranchiseUseCase.run(any())).thenReturn(
                    Mono.error(new RuntimeException(UNEXPECTED_FAILURE)));

            webTestClient.post()
                    .uri(franchisesPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {"name": "My Franchise"}
                            """)
                    .exchange()
                    .expectStatus().is5xxServerError();
        }

        @Test
        @DisplayName("should return 5xx when franchise name is null (domain validation fails)")
        void shouldReturn5xxWhenNameIsNull() {
            webTestClient.post()
                    .uri(franchisesPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                            {"name": null}
                            """)
                    .exchange()
                    .expectStatus().is5xxServerError();
        }
    }

    @Nested
    @DisplayName("getFranchiseById – happy path")
    class GetFranchiseByIdHappyPath {

        @Test
        @DisplayName("should return 200 with franchise body when found")
        void shouldReturn200WhenFranchiseFound() {
            Franchise franchise = new Franchise(FRANCHISE_ID, Optional.of("My Franchise"));
            when(getFranchiseByIdUseCase.run(FRANCHISE_ID)).thenReturn(Mono.just(franchise));

            webTestClient.get()
                    .uri(franchisesPath + "/" + FRANCHISE_ID)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.id").isEqualTo(FRANCHISE_ID);
        }
    }

    @Nested
    @DisplayName("getFranchiseById – not found")
    class GetFranchiseByIdNotFound {

        @Test
        @DisplayName("should return 404 when franchise is not found")
        void shouldReturn404WhenNotFound() {
            when(getFranchiseByIdUseCase.run("unknown-id")).thenReturn(Mono.empty());

            webTestClient.get()
                    .uri(franchisesPath + "/unknown-id")
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    @Nested
    @DisplayName("getFranchiseById – error propagation")
    class GetFranchiseByIdErrorPropagation {

        @Test
        @DisplayName("should return 5xx when the use case throws")
        void shouldReturn5xxOnError() {
            when(getFranchiseByIdUseCase.run(FRANCHISE_ID))
                    .thenReturn(Mono.error(new RuntimeException(UNEXPECTED_FAILURE)));

            webTestClient.get()
                    .uri(franchisesPath + "/" + FRANCHISE_ID)
                    .exchange()
                    .expectStatus().is5xxServerError();
        }
    }

    @Nested
    @DisplayName("updateFranchiseName – happy path")
    class UpdateFranchiseNameHappyPath {

        @Test
        @DisplayName("should return 200 with updated franchise body")
        void shouldReturn200WhenUpdated() {
            Franchise updated = new Franchise(FRANCHISE_ID, Optional.of(NEW_NAME));
            when(updateFranchiseNameUseCase.run(FRANCHISE_ID, NEW_NAME)).thenReturn(Mono.just(updated));

            webTestClient.patch()
                    .uri(franchisesPath + "/" + FRANCHISE_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(UPDATE_NAME_BODY)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.id").isEqualTo(FRANCHISE_ID);
        }
    }

    @Nested
    @DisplayName("updateFranchiseName – not found")
    class UpdateFranchiseNameNotFound {

        @Test
        @DisplayName("should return 404 when franchise is not found")
        void shouldReturn404WhenNotFound() {
            when(updateFranchiseNameUseCase.run("unknown-id", NEW_NAME)).thenReturn(Mono.empty());

            webTestClient.patch()
                    .uri(franchisesPath + "/unknown-id")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(UPDATE_NAME_BODY)
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    @Nested
    @DisplayName("updateFranchiseName – error propagation")
    class UpdateFranchiseNameErrorPropagation {

        @Test
        @DisplayName("should return 5xx when the use case throws")
        void shouldReturn5xxOnError() {
            when(updateFranchiseNameUseCase.run(FRANCHISE_ID, NEW_NAME))
                    .thenReturn(Mono.error(new RuntimeException(UNEXPECTED_FAILURE)));

            webTestClient.patch()
                    .uri(franchisesPath + "/" + FRANCHISE_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(UPDATE_NAME_BODY)
                    .exchange()
                    .expectStatus().is5xxServerError();
        }
    }
}
