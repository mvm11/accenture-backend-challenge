package co.com.bancolombia.usecase.product;

import co.com.bancolombia.model.product.Product;
import co.com.bancolombia.model.product.gateways.ProductRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigInteger;

@RequiredArgsConstructor
public class UpdateProductUseCase {

    private final ProductRepository productRepository;

    public Mono<Product> updateName(String franchiseId, String branchId, String productId, String name) {
        return productRepository.updateProductName(franchiseId, branchId, productId, name);
    }

    public Mono<Product> updateStock(String franchiseId, String branchId, String productId, BigInteger stock) {
        return productRepository.updateProductStock(franchiseId, branchId, productId, stock);
    }
}
