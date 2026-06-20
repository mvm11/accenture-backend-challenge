package co.com.bancolombia.model.product.gateways;

import co.com.bancolombia.model.product.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigInteger;

public interface ProductRepository {

    Mono<Product> findProductById(String franchiseId, String branchId, String productId);

    Flux<Product> findProductsByBranchId(String branchId);

    Mono<Product> saveProduct(String franchiseId, Product product);

    Mono<Product> updateProductName(String franchiseId, String branchId, String productId, String name);

    Mono<Product> updateProductStock(String franchiseId, String branchId, String productId, BigInteger stock);

    Mono<Product> deleteProduct(String franchiseId, String branchId, String productId);

}
