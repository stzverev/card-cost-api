package org.stzverev.cardcostapi;

import com.maciejwalkowiak.wiremock.spring.ConfigureWireMock;
import com.maciejwalkowiak.wiremock.spring.EnableWireMock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(classes = {TestUtilConfig.class, CardCostApiApplication.class}, webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
@EnableWireMock({
        @ConfigureWireMock(name = "card-service", property = "app.thirdrpovider.binlist.baseUrl")
})
public class BaseSpringBootTestContainersTest extends BaseTestcontainersTest {

}
