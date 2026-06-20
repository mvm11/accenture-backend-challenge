package co.com.bancolombia.r2dbc.branch.adapter;

import co.com.bancolombia.model.branch.Branch;
import co.com.bancolombia.model.branch.exceptions.InvalidBranchException;
import co.com.bancolombia.model.branch.gateways.BranchRepository;
import co.com.bancolombia.r2dbc.branch.entity.BranchEntity;
import co.com.bancolombia.r2dbc.branch.repository.BranchReactiveRepository;
import co.com.bancolombia.r2dbc.helper.ReactiveAdapterOperations;
import com.fasterxml.uuid.Generators;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Repository
public class BranchReactiveRepositoryAdapter
        extends ReactiveAdapterOperations<Branch, BranchEntity, String, BranchReactiveRepository>
        implements BranchRepository {

    public BranchReactiveRepositoryAdapter(BranchReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, BranchReactiveRepositoryAdapter::toBranch);
    }

    @Override
    public Mono<Branch> saveBranch(String franchiseId, Branch branch) {
        String uuidV7 = Generators.timeBasedEpochGenerator().generate().toString();
        BranchEntity entity = branch.name()
                .map(name -> getBranchEntity(uuidV7, name, franchiseId))
                .orElseThrow(() -> new InvalidBranchException("Branch name must not be empty"));
        return repository.save(entity).map(BranchReactiveRepositoryAdapter::toBranch);
    }

    @Override
    public Mono<Branch> findBranchById(String franchiseId, String branchId) {
        return repository.findByIdAndFranchiseId(branchId, franchiseId)
                .map(BranchReactiveRepositoryAdapter::toBranch);
    }

    @Override
    public Flux<Branch> findBranchesByFranchiseId(String franchiseId) {
        return repository.findAllByFranchiseId(franchiseId)
                .map(BranchReactiveRepositoryAdapter::toBranch);
    }

    @Override
    public Mono<Branch> updateBranchName(String franchiseId, String branchId, String name) {
        return findBranchById(franchiseId, branchId)
                .flatMap(branch -> {
                    BranchEntity updated = new BranchEntity(branch.id(), name, franchiseId, false);
                    return repository.save(updated).map(BranchReactiveRepositoryAdapter::toBranch);
                });
    }

    private static Branch toBranch(BranchEntity entity) {
        return new Branch(entity.getId(), Optional.ofNullable(entity.getName()));
    }

    private static BranchEntity getBranchEntity(String id, String name, String franchiseId) {
        return BranchEntity.builder()
                .id(id)
                .name(name)
                .franchiseId(franchiseId)
                .isNew(true)
                .build();
    }
}
