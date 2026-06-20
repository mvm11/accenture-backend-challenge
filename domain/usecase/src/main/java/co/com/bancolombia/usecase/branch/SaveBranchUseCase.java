package co.com.bancolombia.usecase.branch;

import co.com.bancolombia.model.branch.Branch;
import co.com.bancolombia.model.branch.gateways.BranchRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class SaveBranchUseCase {

    private final BranchRepository branchRepository;

    public Mono<Branch> run(String franchiseId, Branch branch) {
        return branchRepository.saveBranch(franchiseId, branch);
    }
}
