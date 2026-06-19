package co.com.bancolombia.api;

import co.com.bancolombia.api.franchise.FranchiseHandler;
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
class RouterRestTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private SaveFranchiseUseCase saveFranchiseUseCase;

    @Value("${api.paths.franchises}")
    private String franchisesPath;

    private static final String VALID_BODY = """
            {"name": "My Franchise"}
            """;

    @Nested
    @DisplayName("POST /api/v1/franchises – routing")
    class PostFranchisesRoute {

        @Test
        @DisplayName("should route POST to FranchiseHandler and return 201")
        void shouldRouteToHandler() {
            Franchise saved = new Franchise("uuid-1", Optional.of("My Franchise"));
            when(saveFranchiseUseCase.save(any())).thenReturn(Mono.just(saved));

            webTestClient.post()
                    .uri(franchisesPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(VALID_BODY)
                    .exchange()
                    .expectStatus().isCreated();
        }

        @Test
        @DisplayName("should return 404 for GET on the franchises path (functional routing has no method-level 405)")
        void shouldReturn404ForGetMethod() {
            webTestClient.get()
                    .uri(franchisesPath)
                    .exchange()
                    .expectStatus().isNotFound();
        }

        @Test
        @DisplayName("should return 404 for PUT on the franchises path")
        void shouldReturn404ForPutMethod() {
            webTestClient.put()
                    .uri(franchisesPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(VALID_BODY)
                    .exchange()
                    .expectStatus().isNotFound();
        }

        @Test
        @DisplayName("should return 404 for POST to an unregistered path")
        void shouldReturn404ForUnknownPath() {
            webTestClient.post()
                    .uri(franchisesPath + "/nonexistent")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(VALID_BODY)
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }
}
