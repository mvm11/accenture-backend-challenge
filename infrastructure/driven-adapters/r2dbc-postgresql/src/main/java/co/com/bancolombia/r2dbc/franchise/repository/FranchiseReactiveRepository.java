package co.com.bancolombia.r2dbc.franchise.repository;

import co.com.bancolombia.r2dbc.franchise.entity.FranchiseEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface FranchiseReactiveRepository
        extends ReactiveCrudRepository<FranchiseEntity, String>,
        ReactiveQueryByExampleExecutor<FranchiseEntity> {
}