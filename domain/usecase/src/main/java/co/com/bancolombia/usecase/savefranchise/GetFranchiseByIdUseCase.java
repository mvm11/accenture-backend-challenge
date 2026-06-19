package co.com.bancolombia.usecase.savefranchise;

import co.com.bancolombia.model.franchise.Franchise;
import co.com.bancolombia.model.franchise.gateways.FranchiseRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class GetFranchiseByIdUseCase {

    private final FranchiseRepository franchiseRepository;

    public Mono<Franchise> run(String id) {
        return franchiseRepository.findFranchiseById(id);
    }

}
