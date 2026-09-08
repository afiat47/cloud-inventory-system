package com.example.inventory;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class InventoryApiApplicationTests {

	// @ServiceConnection points spring.datasource.* at the container, so the
	// localhost:5432 fallback in application.yml is never used on a build agent
	@Container @ServiceConnection
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

	@Test
	void contextLoads() {
	}

}
