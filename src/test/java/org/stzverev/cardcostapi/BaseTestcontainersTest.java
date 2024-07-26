package org.stzverev.cardcostapi;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public abstract class BaseTestcontainersTest {

    @Container
    protected static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName
            .parse("mongo:4.0.10"))
            .withExposedPorts(27017);

    @BeforeAll
    public static void checkMongoDB() {
        if (!MONGO_DB_CONTAINER.isRunning()) {
            throw new IllegalStateException("MONGO DB container has started with errors.");
        }
    }

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.port", () -> MONGO_DB_CONTAINER.getMappedPort(27017));
    }

}
