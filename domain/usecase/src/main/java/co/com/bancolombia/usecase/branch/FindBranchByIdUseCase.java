package co.com.bancolombia.usecase.branch;

import co.com.bancolombia.model.branch.Branch;
import co.com.bancolombia.model.branch.gateways.BranchRepository;
import co.com.bancolombia.model.product.gateways.ProductRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class FindBranchByIdUseCase {

    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;

    public Mono<Branch> run(String franchiseId, String branchId) {
        return branchRepository.findBranchById(franchiseId, branchId)
                .flatMap(branch -> productRepository.findProductsByBranchId(branchId)
                        .collectList()
                        .map(products -> new Branch(branch.id(), branch.name(), products)));
    }
}