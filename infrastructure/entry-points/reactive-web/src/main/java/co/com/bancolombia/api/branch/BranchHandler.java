package co.com.bancolombia.api.branch;

import co.com.bancolombia.model.branch.Branch;
import co.com.bancolombia.usecase.branch.FindBranchByIdUseCase;
import co.com.bancolombia.usecase.branch.SaveBranchUseCase;
import co.com.bancolombia.usecase.branch.UpdateBranchNameUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BranchHandler {

    private final SaveBranchUseCase saveBranchUseCase;
    private final FindBranchByIdUseCase findBranchByIdUseCase;
    private final UpdateBranchNameUseCase updateBranchNameUseCase;

    public Mono<ServerResponse> createBranch(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        return request.bodyToMono(BranchRequest.class)
                .map(dto -> new Branch(null, Optional.ofNullable(dto.name()), null))
                .flatMap(branch -> saveBranchUseCase.run(franchiseId, branch))
                .flatMap(saved -> ServerResponse.status(201).bodyValue(saved));
    }

    public Mono<ServerResponse> getBranchById(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        String branchId = request.pathVariable("id");
        return findBranchByIdUseCase.run(franchiseId, branchId)
                .flatMap(branch -> ServerResponse.ok().bodyValue(branch))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> updateBranchName(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        String branchId = request.pathVariable("id");
        return request.bodyToMono(BranchRequest.class)
                .flatMap(dto -> updateBranchNameUseCase.run(franchiseId, branchId, dto.name()))
                .flatMap(updated -> ServerResponse.ok().bodyValue(updated))
                .switchIfEmpty(ServerResponse.notFound().build());
    }
}
