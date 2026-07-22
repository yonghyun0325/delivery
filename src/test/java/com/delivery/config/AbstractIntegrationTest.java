package com.delivery.config;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/** 추상 테스트 컨테이너 클래스 상속해서 사용 */
@Testcontainers
public abstract class AbstractIntegrationTest {

    @Autowired
    protected StringRedisTemplate stringRedisTemplate;

    @AfterEach
    void clearRedis() {
        if (stringRedisTemplate.getConnectionFactory() != null) {
            stringRedisTemplate.getConnectionFactory().getConnection().serverCommands().flushAll();
        }
    }

    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    static {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
