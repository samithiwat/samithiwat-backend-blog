package com.samithiwat.blog;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringBootTest(properties = {
		"grpc.server.inProcessName=test-user-application", // Enable inProcess server
		"grpc.server.port=-1", // Disable external server
		"grpc.client.userApplication.address=in-process:test" // Configure the client to connect to the inProcess server
})
@SpringJUnitConfig(classes = {TestConfig.class})
class UserApplicationTests {

	@Test
	void contextLoads() {
	}

}
