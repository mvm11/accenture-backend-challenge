package co.com.bancolombia.model.branch.gateways;

import co.com.bancolombia.model.branch.Branch;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BranchRepository {

    Mono<Branch> findBranchById(String franchiseId, String branchId);
    Flux<Branch> findBranchesByFranchiseId(String franchiseId);
    Mono<Branch> saveBranch(String franchiseId, Branch branch);
    Mono<Branch> updateBranchName(String franchiseId, String branchId, String name);
}
