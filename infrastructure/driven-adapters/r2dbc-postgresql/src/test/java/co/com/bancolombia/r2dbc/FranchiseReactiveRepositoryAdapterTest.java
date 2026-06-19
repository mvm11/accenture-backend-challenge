package co.com.bancolombia.r2dbc;

import co.com.bancolombia.model.franchise.Franchise;
import co.com.bancolombia.r2dbc.franchise.adapter.FranchiseReactiveRepositoryAdapter;
import co.com.bancolombia.r2dbc.franchise.entity.FranchiseEntity;
import co.com.bancolombia.r2dbc.franchise.repository.FranchiseReactiveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FranchiseReactiveRepositoryAdapter")
class FranchiseReactiveRepositoryAdapterTest {

    @InjectMocks
    FranchiseReactiveRepositoryAdapter repositoryAdapter;

    @Mock
    FranchiseReactiveRepository repository;

    @Mock
    ObjectMapper mapper;

    private static final String DOMAIN_ID = "original-id";
    private static final String PERSISTED_ID = "generated-uuid";
    private static final String FRANCHISE_NAME = "My Franchise";

    private Franchise domainFranchise;
    private FranchiseEntity persistedEntity;

    @BeforeEach
    void setUp() {
        domainFranchise = new Franchise(DOMAIN_ID, Optional.of(FRANCHISE_NAME));

        persistedEntity = FranchiseEntity.builder().id(PERSISTED_ID).name(FRANCHISE_NAME).isNew(true).build();
    }

    @Nested
    @DisplayName("saveFranchise – happy path")
    class SaveFranchiseHappyPath {

        @Test
        @DisplayName("should emit a domain object whose id equals the persisted entity id")
        void shouldEmitResultWithPersistedId() {
            when(repository.save(any(FranchiseEntity.class))).thenReturn(Mono.just(persistedEntity));

            StepVerifier.create(repositoryAdapter.saveFranchise(domainFranchise)).assertNext(result -> assertThat(result.id()).as("Returned id must come from the saved entity, not the domain input").isEqualTo(PERSISTED_ID)).verifyComplete();
        }

        @Test
        @DisplayName("should emit a domain object whose name matches the original franchise name")
        void shouldEmitResultWithCorrectName() {
            when(repository.save(any(FranchiseEntity.class))).thenReturn(Mono.just(persistedEntity));

            StepVerifier.create(repositoryAdapter.saveFranchise(domainFranchise)).assertNext(result -> assertThat(result.name()).as("Returned name must equal the persisted entity name").isEqualTo(Optional.of(FRANCHISE_NAME))).verifyComplete();
        }

        @Test
        @DisplayName("should complete the reactive stream after emitting exactly one element")
        void shouldCompleteAfterSingleEmission() {
            when(repository.save(any(FranchiseEntity.class))).thenReturn(Mono.just(persistedEntity));

            StepVerifier.create(repositoryAdapter.saveFranchise(domainFranchise)).expectNextCount(1).verifyComplete();
        }
    }

    @Nested
    @DisplayName("saveFranchise – entity construction contract")
    class EntityConstructionContract {

        @Test
        @DisplayName("should pass an entity with isNew=true to the repository")
        void shouldPassEntityWithIsNewTrue() {
            ArgumentCaptor<FranchiseEntity> captor = ArgumentCaptor.forClass(FranchiseEntity.class);
            when(repository.save(captor.capture())).thenReturn(Mono.just(persistedEntity));

            StepVerifier.create(repositoryAdapter.saveFranchise(domainFranchise)).expectNextCount(1).verifyComplete();

            assertThat(captor.getValue().isNew()).as("Entity must be flagged as new so Spring Data issues an INSERT rather than a SELECT + UPDATE").isTrue();
        }

        @Test
        @DisplayName("should pass an entity whose name matches the domain franchise name")
        void shouldPassEntityWithCorrectName() {
            ArgumentCaptor<FranchiseEntity> captor = ArgumentCaptor.forClass(FranchiseEntity.class);
            when(repository.save(captor.capture())).thenReturn(Mono.just(persistedEntity));

            StepVerifier.create(repositoryAdapter.saveFranchise(domainFranchise)).expectNextCount(1).verifyComplete();

            assertThat(captor.getValue().getName()).as("Entity name must be unwrapped from the Optional before persisting").isEqualTo(FRANCHISE_NAME);
        }

        @Test
        @DisplayName("should invoke repository.save exactly once per call")
        void shouldInvokeRepositorySaveExactlyOnce() {
            when(repository.save(any(FranchiseEntity.class))).thenReturn(Mono.just(persistedEntity));

            StepVerifier.create(repositoryAdapter.saveFranchise(domainFranchise)).expectNextCount(1).verifyComplete();

            verify(repository, times(1)).save(any(FranchiseEntity.class));
            verifyNoMoreInteractions(repository);
        }
    }

    @Nested
    @DisplayName("saveFranchise – error propagation")
    class ErrorPropagation {

        @Test
        @DisplayName("should propagate a RuntimeException thrown by the repository")
        void shouldPropagateRuntimeException() {
            RuntimeException dbError = new RuntimeException("DB error");
            when(repository.save(any(FranchiseEntity.class))).thenReturn(Mono.error(dbError));

            StepVerifier.create(repositoryAdapter.saveFranchise(domainFranchise)).expectErrorSatisfies(ex -> {
                assertThat(ex).isInstanceOf(RuntimeException.class);
                assertThat(ex.getMessage()).isEqualTo("DB error");
            }).verify();
        }

        @Test
        @DisplayName("should not swallow errors – stream must terminate with an error signal, not complete")
        void shouldTerminateWithErrorNotComplete() {
            when(repository.save(any(FranchiseEntity.class))).thenReturn(Mono.error(new RuntimeException("unexpected failure")));

            StepVerifier.create(repositoryAdapter.saveFranchise(domainFranchise)).expectError(RuntimeException.class).verify();
        }
    }

    @Nested
    @DisplayName("saveFranchise – edge cases")
    class EdgeCases {

        @Test
        @DisplayName("should emit an empty stream when the repository returns Mono.empty()")
        void shouldHandleEmptyMonoFromRepository() {
            when(repository.save(any(FranchiseEntity.class))).thenReturn(Mono.empty());

            StepVerifier.create(repositoryAdapter.saveFranchise(domainFranchise)).verifyComplete();
        }
    }
}