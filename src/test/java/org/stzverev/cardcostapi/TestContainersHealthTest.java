package org.stzverev.cardcostapi;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestContainersHealthTest extends BaseTestcontainersTest {

    @Test
    void isMongoDbRunning() {
        assertTrue(MONGO_DB_CONTAINER.isRunning());
    }

    @Test
    @Disabled
    void isMongoDbAccessible() {
        assertTrue(MONGO_DB_CONTAINER.isHostAccessible());
    }

}
