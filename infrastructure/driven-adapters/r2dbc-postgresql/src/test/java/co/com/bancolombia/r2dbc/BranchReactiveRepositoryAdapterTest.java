package co.com.bancolombia.r2dbc;

import co.com.bancolombia.model.branch.Branch;
import co.com.bancolombia.model.branch.exceptions.InvalidBranchException;
import co.com.bancolombia.r2dbc.branch.adapter.BranchReactiveRepositoryAdapter;
import co.com.bancolombia.r2dbc.branch.entity.BranchEntity;
import co.com.bancolombia.r2dbc.branch.repository.BranchReactiveRepository;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BranchReactiveRepositoryAdapter")
class BranchReactiveRepositoryAdapterTest {

    @InjectMocks
    BranchReactiveRepositoryAdapter repositoryAdapter;

    @Mock
    BranchReactiveRepository repository;

    @Mock
    ObjectMapper mapper;

    private static final String FRANCHISE_ID = "franchise-1";
    private static final String BRANCH_ID = "branch-1";
    private static final String PERSISTED_ID = "generated-uuid";
    private static final String BRANCH_NAME = "My Branch";
    private static final String NEW_NAME = "Updated Name";
    private static final String UNKNOWN_ID = "unknown-id";
    private static final String DB_ERROR_MSG = "DB error";

    private Branch domainBranch;
    private BranchEntity persistedEntity;

    @BeforeEach
    void setUp() {
        domainBranch = new Branch(BRANCH_ID, Optional.of(BRANCH_NAME), null);
        persistedEntity = BranchEntity.builder()
                .id(PERSISTED_ID)
                .name(BRANCH_NAME)
                .franchiseId(FRANCHISE_ID)
                .isNew(true)
                .build();
    }

    @Nested
    @DisplayName("saveBranch – happy path")
    class SaveBranchHappyPath {

        @Test
        @DisplayName("should emit a domain object whose id equals the persisted entity id")
        void shouldEmitResultWithPersistedId() {
            when(repository.save(any(BranchEntity.class))).thenReturn(Mono.just(persistedEntity));

            StepVerifier.create(repositoryAdapter.saveBranch(FRANCHISE_ID, domainBranch))
                    .assertNext(result -> assertThat(result.id()).isEqualTo(PERSISTED_ID))
                    .verifyComplete();
        }

        @Test
        @DisplayName("should emit a domain object whose name matches the original branch name")
        void shouldEmitResultWithCorrectName() {
            when(repository.save(any(BranchEntity.class))).thenReturn(Mono.just(persistedEntity));

            StepVerifier.create(repositoryAdapter.saveBranch(FRANCHISE_ID, domainBranch))
                    .assertNext(result -> assertThat(result.name()).isEqualTo(Optional.of(BRANCH_NAME)))
                    .verifyComplete();
        }

        @Test
        @DisplayName("should complete the reactive stream after emitting exactly one element")
        void shouldCompleteAfterSingleEmission() {
            when(repository.save(any(BranchEntity.class))).thenReturn(Mono.just(persistedEntity));

            StepVerifier.create(repositoryAdapter.saveBranch(FRANCHISE_ID, domainBranch))
                    .expectNextCount(1)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("saveBranch – entity construction contract")
    class SaveBranchEntityConstruction {

        @Test
        @DisplayName("should pass an entity with isNew=true to the repository")
        void shouldPassEntityWithIsNewTrue() {
            ArgumentCaptor<BranchEntity> captor = ArgumentCaptor.forClass(BranchEntity.class);
            when(repository.save(captor.capture())).thenReturn(Mono.just(persistedEntity));

            StepVerifier.create(repositoryAdapter.saveBranch(FRANCHISE_ID, domainBranch))
                    .expectNextCount(1)
                    .verifyComplete();

            assertThat(captor.getValue().isNew())
                    .as("Entity must be flagged as new so Spring Data issues an INSERT")
                    .isTrue();
        }

        @Test
        @DisplayName("should pass an entity with the correct franchise id")
        void shouldPassEntityWithCorrectFranchiseId() {
            ArgumentCaptor<BranchEntity> captor = ArgumentCaptor.forClass(BranchEntity.class);
            when(repository.save(captor.capture())).thenReturn(Mono.just(persistedEntity));

            StepVerifier.create(repositoryAdapter.saveBranch(FRANCHISE_ID, domainBranch))
                    .expectNextCount(1)
                    .verifyComplete();

            assertThat(captor.getValue().getFranchiseId()).isEqualTo(FRANCHISE_ID);
        }

        @Test
        @DisplayName("should pass an entity whose name matches the domain branch name")
        void shouldPassEntityWithCorrectName() {
            ArgumentCaptor<BranchEntity> captor = ArgumentCaptor.forClass(BranchEntity.class);
            when(repository.save(captor.capture())).thenReturn(Mono.just(persistedEntity));

            StepVerifier.create(repositoryAdapter.saveBranch(FRANCHISE_ID, domainBranch))
                    .expectNextCount(1)
                    .verifyComplete();

            assertThat(captor.getValue().getName()).isEqualTo(BRANCH_NAME);
        }

        @Test
        @DisplayName("should invoke repository.save exactly once per call")
        void shouldInvokeRepositorySaveExactlyOnce() {
            when(repository.save(any(BranchEntity.class))).thenReturn(Mono.just(persistedEntity));

            StepVerifier.create(repositoryAdapter.saveBranch(FRANCHISE_ID, domainBranch))
                    .expectNextCount(1)
                    .verifyComplete();

            verify(repository, times(1)).save(any(BranchEntity.class));
            verifyNoMoreInteractions(repository);
        }

        @Test
        @DisplayName("should throw InvalidBranchException when branch name optional is empty")
        void shouldThrowWhenBranchNameIsEmpty() {
            Branch mockBranch = mock(Branch.class);
            when(mockBranch.name()).thenReturn(Optional.empty());

            assertThrows(InvalidBranchException.class,
                    () -> repositoryAdapter.saveBranch(FRANCHISE_ID, mockBranch));
        }
    }

    @Nested
    @DisplayName("saveBranch – error propagation")
    class SaveBranchErrorPropagation {

        @Test
        @DisplayName("should propagate a RuntimeException thrown by the repository")
        void shouldPropagateRuntimeException() {
            RuntimeException dbError = new RuntimeException(DB_ERROR_MSG);
            when(repository.save(any(BranchEntity.class))).thenReturn(Mono.error(dbError));

            StepVerifier.create(repositoryAdapter.saveBranch(FRANCHISE_ID, domainBranch))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(RuntimeException.class);
                        assertThat(ex.getMessage()).isEqualTo(DB_ERROR_MSG);
                    })
                    .verify();
        }

        @Test
        @DisplayName("should emit an empty stream when the repository returns Mono.empty()")
        void shouldHandleEmptyMonoFromRepository() {
            when(repository.save(any(BranchEntity.class))).thenReturn(Mono.empty());

            StepVerifier.create(repositoryAdapter.saveBranch(FRANCHISE_ID, domainBranch))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("findBranchById")
    class FindBranchById {

        @Test
        @DisplayName("should return mapped domain object when entity is found")
        void shouldReturnMappedDomainObjectWhenFound() {
            BranchEntity entity = BranchEntity.builder()
                    .id(BRANCH_ID).name(BRANCH_NAME).franchiseId(FRANCHISE_ID).build();
            when(repository.findByIdAndFranchiseId(BRANCH_ID, FRANCHISE_ID)).thenReturn(Mono.just(entity));

            StepVerifier.create(repositoryAdapter.findBranchById(FRANCHISE_ID, BRANCH_ID))
                    .assertNext(result -> {
                        assertThat(result.id()).isEqualTo(BRANCH_ID);
                        assertThat(result.name()).isEqualTo(Optional.of(BRANCH_NAME));
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("should return empty stream when entity is not found")
        void shouldReturnEmptyWhenNotFound() {
            when(repository.findByIdAndFranchiseId(UNKNOWN_ID, FRANCHISE_ID)).thenReturn(Mono.empty());

            StepVerifier.create(repositoryAdapter.findBranchById(FRANCHISE_ID, UNKNOWN_ID))
                    .verifyComplete();
        }

        @Test
        @DisplayName("should propagate error from repository")
        void shouldPropagateError() {
            RuntimeException error = new RuntimeException(DB_ERROR_MSG);
            when(repository.findByIdAndFranchiseId(BRANCH_ID, FRANCHISE_ID)).thenReturn(Mono.error(error));

            StepVerifier.create(repositoryAdapter.findBranchById(FRANCHISE_ID, BRANCH_ID))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(RuntimeException.class);
                        assertThat(ex.getMessage()).isEqualTo(DB_ERROR_MSG);
                    })
                    .verify();
        }
    }

    @Nested
    @DisplayName("findBranchesByFranchiseId")
    class FindBranchesByFranchiseId {

        @Test
        @DisplayName("should return all branches belonging to the franchise")
        void shouldReturnAllBranchesForFranchise() {
            BranchEntity entity1 = BranchEntity.builder().id("b-1").name("Downtown").franchiseId(FRANCHISE_ID).build();
            BranchEntity entity2 = BranchEntity.builder().id("b-2").name("Uptown").franchiseId(FRANCHISE_ID).build();
            when(repository.findAllByFranchiseId(FRANCHISE_ID)).thenReturn(Flux.just(entity1, entity2));

            StepVerifier.create(repositoryAdapter.findBranchesByFranchiseId(FRANCHISE_ID))
                    .assertNext(b -> assertThat(b.id()).isEqualTo("b-1"))
                    .assertNext(b -> assertThat(b.id()).isEqualTo("b-2"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("should return empty stream when franchise has no branches")
        void shouldReturnEmptyWhenNoBranches() {
            when(repository.findAllByFranchiseId(FRANCHISE_ID)).thenReturn(Flux.empty());

            StepVerifier.create(repositoryAdapter.findBranchesByFranchiseId(FRANCHISE_ID))
                    .verifyComplete();
        }

        @Test
        @DisplayName("should propagate error from repository")
        void shouldPropagateError() {
            RuntimeException error = new RuntimeException(DB_ERROR_MSG);
            when(repository.findAllByFranchiseId(FRANCHISE_ID)).thenReturn(Flux.error(error));

            StepVerifier.create(repositoryAdapter.findBranchesByFranchiseId(FRANCHISE_ID))
                    .expectErrorSatisfies(ex -> assertThat(ex.getMessage()).isEqualTo(DB_ERROR_MSG))
                    .verify();
        }
    }

    @Nested
    @DisplayName("updateBranchName")
    class UpdateBranchName {

        @Test
        @DisplayName("should find branch then save with updated name")
        void shouldUpdateBranchNameSuccessfully() {
            BranchEntity original = BranchEntity.builder()
                    .id(BRANCH_ID).name(BRANCH_NAME).franchiseId(FRANCHISE_ID).build();
            BranchEntity updated = BranchEntity.builder()
                    .id(BRANCH_ID).name(NEW_NAME).franchiseId(FRANCHISE_ID).build();
            when(repository.findByIdAndFranchiseId(BRANCH_ID, FRANCHISE_ID)).thenReturn(Mono.just(original));
            when(repository.save(any(BranchEntity.class))).thenReturn(Mono.just(updated));

            StepVerifier.create(repositoryAdapter.updateBranchName(FRANCHISE_ID, BRANCH_ID, NEW_NAME))
                    .assertNext(result -> {
                        assertThat(result.id()).isEqualTo(BRANCH_ID);
                        assertThat(result.name()).isEqualTo(Optional.of(NEW_NAME));
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("should save entity with isNew=false and updated name")
        void shouldSaveWithIsNewFalseAndNewName() {
            BranchEntity original = BranchEntity.builder()
                    .id(BRANCH_ID).name(BRANCH_NAME).franchiseId(FRANCHISE_ID).build();
            BranchEntity updated = BranchEntity.builder()
                    .id(BRANCH_ID).name(NEW_NAME).franchiseId(FRANCHISE_ID).build();
            ArgumentCaptor<BranchEntity> captor = ArgumentCaptor.forClass(BranchEntity.class);
            when(repository.findByIdAndFranchiseId(BRANCH_ID, FRANCHISE_ID)).thenReturn(Mono.just(original));
            when(repository.save(captor.capture())).thenReturn(Mono.just(updated));

            StepVerifier.create(repositoryAdapter.updateBranchName(FRANCHISE_ID, BRANCH_ID, NEW_NAME))
                    .expectNextCount(1)
                    .verifyComplete();

            assertThat(captor.getValue().isNew()).isFalse();
            assertThat(captor.getValue().getName()).isEqualTo(NEW_NAME);
            assertThat(captor.getValue().getFranchiseId()).isEqualTo(FRANCHISE_ID);
        }

        @Test
        @DisplayName("should return empty stream when branch is not found")
        void shouldReturnEmptyWhenBranchNotFound() {
            when(repository.findByIdAndFranchiseId(UNKNOWN_ID, FRANCHISE_ID)).thenReturn(Mono.empty());

            StepVerifier.create(repositoryAdapter.updateBranchName(FRANCHISE_ID, UNKNOWN_ID, NEW_NAME))
                    .verifyComplete();
        }

        @Test
        @DisplayName("should propagate error when find fails")
        void shouldPropagateErrorWhenFindFails() {
            RuntimeException error = new RuntimeException(DB_ERROR_MSG);
            when(repository.findByIdAndFranchiseId(BRANCH_ID, FRANCHISE_ID)).thenReturn(Mono.error(error));

            StepVerifier.create(repositoryAdapter.updateBranchName(FRANCHISE_ID, BRANCH_ID, NEW_NAME))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(RuntimeException.class);
                        assertThat(ex.getMessage()).isEqualTo(DB_ERROR_MSG);
                    })
                    .verify();
        }

        @Test
        @DisplayName("should propagate error when save fails")
        void shouldPropagateErrorWhenSaveFails() {
            BranchEntity original = BranchEntity.builder()
                    .id(BRANCH_ID).name(BRANCH_NAME).franchiseId(FRANCHISE_ID).build();
            RuntimeException error = new RuntimeException("Save failed");
            when(repository.findByIdAndFranchiseId(BRANCH_ID, FRANCHISE_ID)).thenReturn(Mono.just(original));
            when(repository.save(any(BranchEntity.class))).thenReturn(Mono.error(error));

            StepVerifier.create(repositoryAdapter.updateBranchName(FRANCHISE_ID, BRANCH_ID, NEW_NAME))
                    .expectErrorSatisfies(ex -> {
                        assertThat(ex).isInstanceOf(RuntimeException.class);
                        assertThat(ex.getMessage()).isEqualTo("Save failed");
                    })
                    .verify();
        }
    }
}
