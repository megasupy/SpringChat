package com.example.demo.Repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class UserRepositoryJdbcBindingSafetyTest {

	@Mock
	private JdbcTemplate template;

	@InjectMocks
	private UserRepository subject;

	@Captor
	private ArgumentCaptor<String> sqlCaptor;

	private String nastyUsername;

	@BeforeEach
	void setUp() {
		nastyUsername = "admin'; DELETE FROM users;--";
	}

	@Test
	void insert_bindsViaParametersNotSqlInterpolation() {
		UUID inserted = UUID.randomUUID();

		when(template.queryForObject(
				anyString(),
				eq(UUID.class),
				eq(nastyUsername),
				eq("hash_here")))
				.thenReturn(inserted);

		UUID returned = subject.insert(nastyUsername, "hash_here");

		assertEquals(inserted, returned);

		verify(template).queryForObject(
				sqlCaptor.capture(),
				eq(UUID.class),
				eq(nastyUsername),
				eq("hash_here"));
		String sql = sqlCaptor.getValue();

		assertTrue(sql.contains("?"), sql);
		assertFalse(sql.contains(nastyUsername), sql);
	}
}
