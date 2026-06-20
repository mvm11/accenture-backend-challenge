package co.com.bancolombia.usecase.franchise;

import co.com.bancolombia.model.branch.TopStockProduct;
import co.com.bancolombia.model.branch.gateways.BranchRepository;
import co.com.bancolombia.model.franchise.exceptions.FranchiseNotFoundException;
import co.com.bancolombia.model.franchise.gateways.FranchiseRepository;
import co.com.bancolombia.model.product.gateways.ProductRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class GetTopStockProductPerBranchUseCase {

    private final FranchiseRepository franchiseRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;

    public Flux<TopStockProduct> run(String franchiseId) {
        return franchiseRepository.findFranchiseById(franchiseId)
                .switchIfEmpty(Mono.error(new FranchiseNotFoundException("Franchise not found: " + franchiseId)))
                .flatMapMany(franchise -> branchRepository.findBranchesByFranchiseId(franchiseId))
                .flatMap(branch -> productRepository.findProductsByBranchId(branch.id())
                        .reduce((p1, p2) -> p1.stock().compareTo(p2.stock()) >= 0 ? p1 : p2)
                        .map(product -> new TopStockProduct(
                                branch.id(),
                                branch.name().orElse(null),
                                product.id(),
                                product.name(),
                                product.stock())));
    }
}
