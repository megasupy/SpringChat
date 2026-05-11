package com.example.demo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Full context requires Postgres; see unit/WebMvc slice tests")
class DemoApplicationTests {

	@Test
	void contextLoads() {
	}

}
