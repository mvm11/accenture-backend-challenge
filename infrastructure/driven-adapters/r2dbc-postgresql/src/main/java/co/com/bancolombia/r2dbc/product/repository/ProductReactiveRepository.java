package co.com.bancolombia.r2dbc.product.repository;

import co.com.bancolombia.r2dbc.product.entity.ProductEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductReactiveRepository extends ReactiveCrudRepository<ProductEntity, String>,
        ReactiveQueryByExampleExecutor<ProductEntity> {

    Mono<ProductEntity> findByIdAndBranchId(String id, String branchId);
    Flux<ProductEntity> findAllByBranchId(String branchId);
}
