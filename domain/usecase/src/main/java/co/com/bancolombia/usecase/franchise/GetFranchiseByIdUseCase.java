package co.com.bancolombia.usecase.franchise;

import co.com.bancolombia.model.branch.Branch;
import co.com.bancolombia.model.branch.gateways.BranchRepository;
import co.com.bancolombia.model.franchise.Franchise;
import co.com.bancolombia.model.franchise.gateways.FranchiseRepository;
import co.com.bancolombia.model.product.gateways.ProductRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class GetFranchiseByIdUseCase {

    private final FranchiseRepository franchiseRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;

    public Mono<Franchise> run(String id) {
        return franchiseRepository.findFranchiseById(id)
                .flatMap(franchise -> branchRepository.findBranchesByFranchiseId(id)
                        .flatMap(branch -> productRepository.findProductsByBranchId(branch.id())
                                .collectList()
                                .map(products -> new Branch(branch.id(), branch.name(), products)))
                        .collectList()
                        .map(branches -> new Franchise(franchise.id(), franchise.name(), branches)));
    }
}
