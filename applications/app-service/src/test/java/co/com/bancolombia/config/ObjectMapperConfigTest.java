package co.com.bancolombia.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.reactivecommons.utils.ObjectMapper;
import org.reactivecommons.utils.ObjectMapperImp;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.*;

class ObjectMapperConfigTest {

    private ObjectMapperConfig config;

    @BeforeEach
    void setUp() {
        config = new ObjectMapperConfig();
    }

    @Nested
    @DisplayName("objectMapper bean – unit")
    class UnitTests {

        @Test
        @DisplayName("should return a non-null instance")
        void shouldReturnNonNull() {
            assertNotNull(config.objectMapper());
        }

        @Test
        @DisplayName("should return an ObjectMapperImp instance")
        void shouldReturnObjectMapperImp() {
            assertInstanceOf(ObjectMapperImp.class, config.objectMapper());
        }

        @Test
        @DisplayName("should return a new instance on each call")
        void shouldReturnNewInstanceOnEachCall() {
            ObjectMapper first = config.objectMapper();
            ObjectMapper second = config.objectMapper();
            assertNotSame(first, second);
        }
    }

    @Nested
    @DisplayName("objectMapper bean – Spring context")
    class SpringContextTests {

        @Test
        @DisplayName("should register ObjectMapper bean in the application context")
        void shouldRegisterBeanInContext() {
            try (AnnotationConfigApplicationContext context =
                         new AnnotationConfigApplicationContext(ObjectMapperConfig.class)) {
                ObjectMapper bean = context.getBean(ObjectMapper.class);
                assertNotNull(bean);
                assertInstanceOf(ObjectMapperImp.class, bean);
            }
        }
    }

    @Nested
    @DisplayName("objectMapper bean – functional")
    class FunctionalTests {

        @Test
        @DisplayName("should map fields from source to destination by name")
        void shouldMapFieldsByName() {
            ObjectMapper mapper = config.objectMapper();
            Source source = new Source("test-id", "test-name");

            Destination destination = mapper.map(source, Destination.class);

            assertEquals("test-id", destination.getId());
            assertEquals("test-name", destination.getName());
        }

        @Test
        @DisplayName("should return null fields in destination when source fields are null")
        void shouldMapNullFields() {
            ObjectMapper mapper = config.objectMapper();
            Source source = new Source(null, null);

            Destination destination = mapper.map(source, Destination.class);

            assertNull(destination.getId());
            assertNull(destination.getName());
        }
    }

    static class Source {
        private String id;
        private String name;

        Source(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() { return id; }
        public String getName() { return name; }
    }

    static class Destination {
        private String id;
        private String name;

        public String getId() { return id; }
        public String getName() { return name; }
        public void setId(String id) { this.id = id; }
        public void setName(String name) { this.name = name; }
    }
}
