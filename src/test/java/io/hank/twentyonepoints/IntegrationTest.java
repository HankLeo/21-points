package io.hank.twentyonepoints;

import io.hank.twentyonepoints.config.AsyncSyncConfiguration;
import io.hank.twentyonepoints.config.EmbeddedElasticsearch;
import io.hank.twentyonepoints.config.EmbeddedKafka;
import io.hank.twentyonepoints.config.EmbeddedRedis;
import io.hank.twentyonepoints.config.EmbeddedSQL;
import io.hank.twentyonepoints.config.JacksonConfiguration;
import io.hank.twentyonepoints.config.TestSecurityConfiguration;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(
    classes = { TwentyOnePointsApp.class, JacksonConfiguration.class, AsyncSyncConfiguration.class, TestSecurityConfiguration.class }
)
@EmbeddedRedis
@EmbeddedElasticsearch
@EmbeddedSQL
@EmbeddedKafka
public @interface IntegrationTest {
}
