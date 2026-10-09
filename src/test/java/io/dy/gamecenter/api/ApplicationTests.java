package io.dy.gamecenter.api;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Boots the whole application, which means it needs the database and the cache the application
 * talks to, so it is an integration test and not part of `mvn test` (see the surefire
 * configuration: the "integration" group is excluded from the default run).
 *
 *   docker compose up -d
 *   ./mvnw -B test -Dgroups=integration
 */
@Tag("integration")
@SpringBootTest
class ApplicationTests {

	@Test
	void contextLoads() {
	}

}
