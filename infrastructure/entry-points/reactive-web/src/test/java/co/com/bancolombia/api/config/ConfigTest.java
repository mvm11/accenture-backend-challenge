package co.com.bancolombia.api.config;

import co.com.bancolombia.api.RouterRest;
import co.com.bancolombia.api.branch.BranchHandler;
import co.com.bancolombia.api.franchise.FranchiseHandler;
import co.com.bancolombia.usecase.branch.FindBranchByIdUseCase;
import co.com.bancolombia.usecase.branch.SaveBranchUseCase;
import co.com.bancolombia.usecase.branch.UpdateBranchNameUseCase;
import co.com.bancolombia.usecase.franchise.GetFranchiseByIdUseCase;
import co.com.bancolombia.usecase.franchise.SaveFranchiseUseCase;
import co.com.bancolombia.usecase.franchise.UpdateFranchiseNameUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest
@ContextConfiguration(classes = {RouterRest.class, FranchiseHandler.class, BranchHandler.class})
@Import({CorsConfig.class, SecurityHeadersConfig.class})
class ConfigTest {

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

    @Test
    @DisplayName("SecurityHeadersConfig filter should add security headers to every response")
    void securityHeadersShouldBePresentOnAnyResponse() {
        webTestClient.get()
                .uri("/unknown-path")
                .exchange()
                .expectStatus().isNotFound()
                .expectHeader().valueEquals("Content-Security-Policy",
                        "default-src 'self'; frame-ancestors 'self'; form-action 'self'")
                .expectHeader().valueEquals("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload")
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
                .expectHeader().valueEquals("Server", "")
                .expectHeader().valueEquals("Cache-Control", "no-store")
                .expectHeader().valueEquals("Pragma", "no-cache")
                .expectHeader().valueEquals("Referrer-Policy", "strict-origin-when-cross-origin");
    }
}
