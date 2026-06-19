package co.com.bancolombia.api.franchise;

import co.com.bancolombia.api.RouterRest;
import co.com.bancolombia.model.franchise.Franchise;
import co.com.bancolombia.usecase.savefranchise.SaveFranchiseUseCase;
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

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private SaveFranchiseUseCase saveFranchiseUseCase;

    @Value("${api.paths.franchises}")
    private String franchisesPath;

    @Nested
    @DisplayName("createFranchise – happy path")
    class HappyPath {

        @Test
        @DisplayName("should return 201 when franchise is created successfully")
        void shouldReturn201OnSuccess() {
            Franchise saved = new Franchise("uuid-1", Optional.of("My Franchise"));
            when(saveFranchiseUseCase.save(any())).thenReturn(Mono.just(saved));

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
            when(saveFranchiseUseCase.save(any(Franchise.class))).thenReturn(Mono.just(saved));

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
            when(saveFranchiseUseCase.save(any())).thenReturn(
                    Mono.error(new RuntimeException("unexpected failure")));

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
}
