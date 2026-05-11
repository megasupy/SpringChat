package com.example.demo.Services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.Repositories.MessageRepository;
import com.example.demo.Repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class MessageTextValidationTest {

	@Mock
	private MessageRepository messageRepository;

	@Mock
	private UserRepository userRepository;

	private MessageService newService() {
		return new MessageService(messageRepository, userRepository);
	}

	@ParameterizedTest
	@CsvSource({
			// Current implementation uses isEmpty(): only "" is rejected; whitespace-only counts as valid.
			"'',false",
			"'   ',true",
			"'\n',true",
			"' \n ',true",
			"'hello',true",
			"'  trimmed still text  ',true"
	})
	void isValidMessageText(String raw, boolean expected) {
		String text = raw == null ? "" : raw.replace("\\n", "\n");
		MessageService service = newService();
		assertEquals(expected, service.isValidMessageText(text));
	}
}

