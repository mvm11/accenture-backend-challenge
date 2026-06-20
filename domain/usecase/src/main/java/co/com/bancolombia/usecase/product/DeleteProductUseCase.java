package co.com.bancolombia.usecase.product;

import co.com.bancolombia.model.product.Product;
import co.com.bancolombia.model.product.gateways.ProductRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class DeleteProductUseCase {

    private final ProductRepository productRepository;

    public Mono<Product> run(String franchiseId, String branchId, String productId) {
        return productRepository.deleteProduct(franchiseId, branchId, productId);
    }
}
