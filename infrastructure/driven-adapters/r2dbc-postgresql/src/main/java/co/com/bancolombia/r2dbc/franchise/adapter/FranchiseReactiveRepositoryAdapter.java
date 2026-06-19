package co.com.bancolombia.r2dbc.franchise.adapter;

import co.com.bancolombia.model.franchise.Franchise;
import co.com.bancolombia.model.franchise.exceptions.InvalidFranchiseException;
import co.com.bancolombia.model.franchise.gateways.FranchiseRepository;
import co.com.bancolombia.r2dbc.franchise.entity.FranchiseEntity;
import co.com.bancolombia.r2dbc.franchise.repository.FranchiseReactiveRepository;
import co.com.bancolombia.r2dbc.helper.ReactiveAdapterOperations;
import com.fasterxml.uuid.Generators;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Repository
public class FranchiseReactiveRepositoryAdapter
        extends ReactiveAdapterOperations<Franchise, FranchiseEntity, String, FranchiseReactiveRepository>
        implements FranchiseRepository {

    public FranchiseReactiveRepositoryAdapter(FranchiseReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, FranchiseReactiveRepositoryAdapter::toFranchise);
    }


    @Override
    public Mono<Franchise> saveFranchise(Franchise franchise) {
        String uuidV7 = Generators.timeBasedEpochGenerator().generate().toString();
        FranchiseEntity entity = franchise.name()
                .map(name -> getFranchiseEntity(name, uuidV7))
                .orElseThrow(() -> new InvalidFranchiseException("Franchise name must not be empty"));
        return repository.save(entity)
                .map(FranchiseReactiveRepositoryAdapter::toFranchise);
    }

    @Override
    public Mono<Franchise> updateFranchiseName(String id, String name) {
        return findFranchiseById(id)
                .flatMap(franchise -> updateFranchiseName(name, franchise));
    }

    private Mono<Franchise> updateFranchiseName(String name, Franchise franchise) {
        FranchiseEntity updatedFranchise = new FranchiseEntity(franchise.id(), name, false);
        return repository.save(updatedFranchise).map(FranchiseReactiveRepositoryAdapter::toFranchise);
    }

    @Override
    public Mono<Franchise> findFranchiseById(String id) {
        return repository.findById(id)
                .map(FranchiseReactiveRepositoryAdapter::toFranchise);
    }

    private static Franchise toFranchise(FranchiseEntity entity) {
        return new Franchise(entity.getId(), Optional.ofNullable(entity.getName()));
    }

    private static FranchiseEntity getFranchiseEntity(String name, String uuidV7) {
        return FranchiseEntity.builder()
                .id(uuidV7)
                .name(name)
                .isNew(true)
                .build();
    }
}