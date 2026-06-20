package co.com.bancolombia.usecase.product;

import co.com.bancolombia.model.product.Product;
import co.com.bancolombia.model.product.gateways.ProductRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class SaveProductUseCase {

    private final ProductRepository productRepository;

    public Mono<Product> run(String branchId, Product product) {
        return productRepository.saveProduct(branchId, product);
    }
}
