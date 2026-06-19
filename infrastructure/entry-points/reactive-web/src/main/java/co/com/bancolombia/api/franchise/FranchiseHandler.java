package co.com.bancolombia.api.franchise;

import co.com.bancolombia.model.franchise.Franchise;
import co.com.bancolombia.usecase.savefranchise.GetFranchiseByIdUseCase;
import co.com.bancolombia.usecase.savefranchise.SaveFranchiseUseCase;
import co.com.bancolombia.usecase.savefranchise.UpdateFranchiseNameUseCase;
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
    private final GetFranchiseByIdUseCase getFranchiseByIdUseCase;
    private final UpdateFranchiseNameUseCase updateFranchiseNameUseCase;

    public Mono<ServerResponse> createFranchise(ServerRequest request) {
        return request.bodyToMono(FranchiseRequest.class)
                .map(dto -> new Franchise(null, Optional.ofNullable(dto.name())))
                .flatMap(saveFranchiseUseCase::run)
                .flatMap(saved -> ServerResponse.status(201).bodyValue(saved));
    }

    public Mono<ServerResponse> getFranchiseById(ServerRequest request) {
        String id = request.pathVariable("id");
        return getFranchiseByIdUseCase.run(id)
                .flatMap(franchise -> ServerResponse.ok().bodyValue(franchise))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> updateFranchiseName(ServerRequest request) {
        String id = request.pathVariable("id");
        return request.bodyToMono(FranchiseRequest.class)
                .flatMap(dto -> updateFranchiseNameUseCase.run(id, dto.name()))
                .flatMap(updated -> ServerResponse.ok().bodyValue(updated))
                .switchIfEmpty(ServerResponse.notFound().build());
    }
}