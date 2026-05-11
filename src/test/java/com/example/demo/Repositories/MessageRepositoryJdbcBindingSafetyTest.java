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

import com.example.demo.Models.Message;

@ExtendWith(MockitoExtension.class)
class MessageRepositoryJdbcBindingSafetyTest {

	@Mock
	private JdbcTemplate template;

	@InjectMocks
	private MessageRepository subject;

	private Message msg;

	@Captor
	private ArgumentCaptor<String> sqlCaptor;

	@BeforeEach
	void setMsg() {
		msg = new Message();
		msg.sender_id = UUID.randomUUID();
		msg.recipient_id = UUID.randomUUID();
		msg.message = "'); DROP TABLE messages;--";
	}

	@Test
	void insert_usesJdbcPlaceholdersBindingsNotStringConcatSql() {
		when(template.update(anyString(),
				eq(msg.sender_id),
				eq(msg.recipient_id),
				eq(msg.message)))
				.thenReturn(1);

		subject.insert(msg);

		verify(template).update(sqlCaptor.capture(),
				eq(msg.sender_id),
				eq(msg.recipient_id),
				eq(msg.message));
		String sql = sqlCaptor.getValue();

		assertTrue(sql.contains("?"), sql);
		assertFalse(sql.contains(msg.message));
		assertFalse(sql.contains(msg.sender_id.toString()), sql);
	}
}
