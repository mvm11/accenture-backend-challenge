package co.com.bancolombia.api.franchise;

import co.com.bancolombia.model.franchise.Franchise;
import co.com.bancolombia.usecase.savefranchise.SaveFranchiseUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FranchiseHandler {

    private final SaveFranchiseUseCase saveFranchiseUseCase;

    public Mono<ServerResponse> createFranchise(ServerRequest request) {
        return request.bodyToMono(FranchiseRequest.class)
                .map(dto -> new Franchise(null, Optional.ofNullable(dto.name())))
                .flatMap(saveFranchiseUseCase::save)
                .flatMap(saved -> ServerResponse.status(201).bodyValue(saved));
    }
}