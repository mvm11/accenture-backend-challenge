package co.com.bancolombia.usecase.franchise;

import co.com.bancolombia.model.branch.gateways.BranchRepository;
import co.com.bancolombia.model.franchise.Franchise;
import co.com.bancolombia.model.franchise.gateways.FranchiseRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class GetFranchiseByIdUseCase {

    private final FranchiseRepository franchiseRepository;
    private final BranchRepository branchRepository;

    public Mono<Franchise> run(String id) {
        return franchiseRepository.findFranchiseById(id)
                .flatMap(franchise -> branchRepository.findBranchesByFranchiseId(id)
                        .collectList()
                        .map(branches -> new Franchise(franchise.id(), franchise.name(), branches)));
    }
}
