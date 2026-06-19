package co.com.bancolombia.model.franchise.gateways;

import co.com.bancolombia.model.franchise.Franchise;
import reactor.core.publisher.Mono;

public interface FranchiseRepository {

    Mono<Franchise> saveFranchise(Franchise franchise);
    Mono<Franchise> updateFranchiseName(String id, String name);
    Mono<Franchise> findFranchiseById(String id);

}
