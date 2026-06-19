package co.com.bancolombia.r2dbc.helper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.domain.Example;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class ReactiveAdapterOperationsTest {

    private DummyRepository repository;
    private ObjectMapper mapper;
    private TestableAdapter operations;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(DummyRepository.class);
        mapper = Mockito.mock(ObjectMapper.class);
        operations = new TestableAdapter(repository, mapper);
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("should map entity to data, persist it and map the result back")
        void shouldSaveEntity() {
            DummyEntity entity = new DummyEntity("1", "test");
            DummyData data = new DummyData("1", "test");

            when(mapper.map(entity, DummyData.class)).thenReturn(data);
            when(repository.save(data)).thenReturn(Mono.just(data));

            StepVerifier.create(operations.save(entity))
                    .expectNext(entity)
                    .verifyComplete();
        }

        @Test
        @DisplayName("should propagate repository error")
        void shouldPropagateError() {
            DummyEntity entity = new DummyEntity("1", "test");
            DummyData data = new DummyData("1", "test");

            when(mapper.map(entity, DummyData.class)).thenReturn(data);
            when(repository.save(data)).thenReturn(Mono.error(new RuntimeException("save failed")));

            StepVerifier.create(operations.save(entity))
                    .expectErrorMessage("save failed")
                    .verify();
        }
    }

    @Nested
    @DisplayName("saveAllEntities")
    class SaveAllEntities {

        @Test
        @DisplayName("should persist all entities and return mapped results")
        void shouldSaveAllEntities() {
            DummyEntity entity1 = new DummyEntity("1", "test1");
            DummyEntity entity2 = new DummyEntity("2", "test2");
            DummyData data1 = new DummyData("1", "test1");
            DummyData data2 = new DummyData("2", "test2");

            when(mapper.map(entity1, DummyData.class)).thenReturn(data1);
            when(mapper.map(entity2, DummyData.class)).thenReturn(data2);
            when(repository.saveAll(any(Flux.class))).thenReturn(Flux.just(data1, data2));

            StepVerifier.create(operations.saveAllEntities(Flux.just(entity1, entity2)))
                    .expectNext(entity1, entity2)
                    .verifyComplete();
        }

        @Test
        @DisplayName("should complete without emitting when flux is empty")
        void shouldCompleteOnEmptyFlux() {
            when(repository.saveAll(any(Flux.class))).thenReturn(Flux.empty());

            StepVerifier.create(operations.saveAllEntities(Flux.empty()))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should return mapped entity when found")
        void shouldReturnEntityWhenFound() {
            DummyData data = new DummyData("1", "test");
            DummyEntity entity = new DummyEntity("1", "test");

            when(repository.findById("1")).thenReturn(Mono.just(data));

            StepVerifier.create(operations.findById("1"))
                    .expectNext(entity)
                    .verifyComplete();
        }

        @Test
        @DisplayName("should return empty Mono when id does not exist")
        void shouldReturnEmptyWhenNotFound() {
            when(repository.findById("missing")).thenReturn(Mono.empty());

            StepVerifier.create(operations.findById("missing"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("should propagate repository error")
        void shouldPropagateError() {
            when(repository.findById("1")).thenReturn(Mono.error(new RuntimeException("db error")));

            StepVerifier.create(operations.findById("1"))
                    .expectErrorMessage("db error")
                    .verify();
        }
    }

    @Nested
    @DisplayName("findByExample")
    class FindByExample {

        @Test
        @DisplayName("should query by example and return mapped entities")
        void shouldFindByExample() {
            DummyEntity entity = new DummyEntity("1", "test");
            DummyData data = new DummyData("1", "test");

            when(mapper.map(entity, DummyData.class)).thenReturn(data);
            when(repository.findAll(any(Example.class))).thenReturn(Flux.just(data));

            StepVerifier.create(operations.findByExample(entity))
                    .expectNext(entity)
                    .verifyComplete();
        }

        @Test
        @DisplayName("should return empty Flux when no matches found")
        void shouldReturnEmptyFluxWhenNoMatches() {
            DummyEntity entity = new DummyEntity("1", "test");
            DummyData data = new DummyData("1", "test");

            when(mapper.map(entity, DummyData.class)).thenReturn(data);
            when(repository.findAll(any(Example.class))).thenReturn(Flux.empty());

            StepVerifier.create(operations.findByExample(entity))
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("should return all mapped entities")
        void shouldReturnAllEntities() {
            DummyData data1 = new DummyData("1", "test1");
            DummyData data2 = new DummyData("2", "test2");
            DummyEntity entity1 = new DummyEntity("1", "test1");
            DummyEntity entity2 = new DummyEntity("2", "test2");

            when(repository.findAll()).thenReturn(Flux.just(data1, data2));

            StepVerifier.create(operations.findAll())
                    .expectNext(entity1, entity2)
                    .verifyComplete();
        }

        @Test
        @DisplayName("should return empty Flux when no entities exist")
        void shouldReturnEmptyFlux() {
            when(repository.findAll()).thenReturn(Flux.empty());

            StepVerifier.create(operations.findAll())
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("toEntity")
    class ToEntity {

        @Test
        @DisplayName("should return null when data is null")
        void shouldReturnNullWhenDataIsNull() {
            assertThat(operations.callToEntity(null)).isNull();
        }

        @Test
        @DisplayName("should apply mapping function when data is not null")
        void shouldApplyMappingFunctionWhenDataIsNotNull() {
            DummyData data = new DummyData("1", "test");
            assertThat(operations.callToEntity(data)).isEqualTo(new DummyEntity("1", "test"));
        }
    }

    // Concrete subclass that exposes protected toEntity for direct testing
    static class TestableAdapter extends ReactiveAdapterOperations<DummyEntity, DummyData, String, DummyRepository> {
        TestableAdapter(DummyRepository repository, ObjectMapper mapper) {
            super(repository, mapper, DummyEntity::toEntity);
        }

        DummyEntity callToEntity(DummyData data) {
            return toEntity(data);
        }
    }

    static class DummyEntity {
        private String id;
        private String name;

        DummyEntity(String id, String name) {
            this.id = id;
            this.name = name;
        }

        static DummyEntity toEntity(DummyData data) {
            return new DummyEntity(data.getId(), data.getName());
        }

        public String getId() { return id; }
        public String getName() { return name; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DummyEntity that = (DummyEntity) o;
            return id.equals(that.id) && name.equals(that.name);
        }

        @Override
        public int hashCode() { return Objects.hash(id, name); }
    }

    static class DummyData {
        private String id;
        private String name;

        DummyData(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() { return id; }
        public String getName() { return name; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DummyData that = (DummyData) o;
            return id.equals(that.id) && name.equals(that.name);
        }

        @Override
        public int hashCode() { return Objects.hash(id, name); }
    }

    interface DummyRepository extends ReactiveCrudRepository<DummyData, String>, ReactiveQueryByExampleExecutor<DummyData> {}
}
