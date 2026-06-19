package co.com.bancolombia.usecase.savefranchise;

import co.com.bancolombia.model.franchise.Franchise;
import co.com.bancolombia.model.franchise.gateways.FranchiseRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class SaveFranchiseUseCase {

    private final FranchiseRepository franchiseRepository;

    public Mono<Franchise> save(Franchise franchise) {
        return franchiseRepository.saveFranchise(franchise);
    }
}