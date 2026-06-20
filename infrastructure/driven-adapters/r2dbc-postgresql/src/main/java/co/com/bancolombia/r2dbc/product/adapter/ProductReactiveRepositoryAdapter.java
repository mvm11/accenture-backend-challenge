package co.com.bancolombia.r2dbc.product.adapter;

import co.com.bancolombia.model.product.Product;
import co.com.bancolombia.model.product.gateways.ProductRepository;
import co.com.bancolombia.r2dbc.helper.ReactiveAdapterOperations;
import co.com.bancolombia.r2dbc.product.entity.ProductEntity;
import co.com.bancolombia.r2dbc.product.repository.ProductReactiveRepository;
import com.fasterxml.uuid.Generators;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigInteger;

@Repository
public class ProductReactiveRepositoryAdapter extends
        ReactiveAdapterOperations<Product, ProductEntity, String, ProductReactiveRepository>
        implements ProductRepository {

    public ProductReactiveRepositoryAdapter(ProductReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, ProductReactiveRepositoryAdapter::toProduct);
    }

    @Override
    public Mono<Product> findProductById(String franchiseId, String branchId, String productId) {
        return repository.findByIdAndBranchId(productId, branchId)
                .map(ProductReactiveRepositoryAdapter::toProduct);
    }

    @Override
    public Flux<Product> findProductsByBranchId(String branchId) {
        return repository.findAllByBranchId(branchId)
                .map(ProductReactiveRepositoryAdapter::toProduct);
    }

    @Override
    public Mono<Product> saveProduct(String branchId, Product product) {
        String uuidV7 = Generators.timeBasedEpochGenerator().generate().toString();
        ProductEntity entity = ProductEntity.builder()
                .id(uuidV7)
                .name(product.name())
                .stock(product.stock())
                .branchId(branchId)
                .isNew(true)
                .build();
        return repository.save(entity).map(ProductReactiveRepositoryAdapter::toProduct);
    }

    @Override
    public Mono<Product> updateProductName(String franchiseId, String branchId, String productId, String name) {
        return findProductById(franchiseId, branchId, productId)
                .flatMap(product -> {
                    ProductEntity updated = new ProductEntity(product.id(), name, product.stock(), branchId, false);
                    return repository.save(updated).map(ProductReactiveRepositoryAdapter::toProduct);
                });
    }

    @Override
    public Mono<Product> updateProductStock(String franchiseId, String branchId, String productId, BigInteger stock) {
        return findProductById(franchiseId, branchId, productId)
                .flatMap(product -> {
                    ProductEntity updated = new ProductEntity(product.id(), product.name(), stock, branchId, false);
                    return repository.save(updated).map(ProductReactiveRepositoryAdapter::toProduct);
                });
    }

    @Override
    public Mono<Product> deleteProduct(String franchiseId, String branchId, String productId) {
        return findProductById(franchiseId, branchId, productId)
                .flatMap(product -> repository.deleteById(product.id()).thenReturn(product));
    }

    private static Product toProduct(ProductEntity entity) {
        return new Product(entity.getId(), entity.getName(), entity.getStock());
    }
}
