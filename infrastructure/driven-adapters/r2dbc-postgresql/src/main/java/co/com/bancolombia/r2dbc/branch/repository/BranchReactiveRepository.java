package co.com.bancolombia.r2dbc.branch.repository;

import co.com.bancolombia.r2dbc.branch.entity.BranchEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BranchReactiveRepository
        extends ReactiveCrudRepository<BranchEntity, String>,
        ReactiveQueryByExampleExecutor<BranchEntity> {

    Mono<BranchEntity> findByIdAndFranchiseId(String id, String franchiseId);
    Flux<BranchEntity> findAllByFranchiseId(String franchiseId);
}
