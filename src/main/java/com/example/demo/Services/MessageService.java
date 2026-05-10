package com.example.demo.Services;

import java.util.List;

import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.stereotype.Service;

import com.example.demo.Repositories.UserRepository;
import com.example.demo.Models.MyUser;
import com.example.demo.Repositories.MessageRepository;
import com.example.demo.Models.Message;
import com.example.demo.AuthHelper;

@Service
public class MessageService {
    private final MessageRepository messageRepository;

    private final UserRepository userRepository;

    public MessageService(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    int maxLimit = 100;

    private boolean userIsAuthorizedToSend(Message msg) {
        // might add a firends list or something later
        MyUser sender = userRepository.get(msg.sender_id);
        String username = AuthHelper.getUserName();
        return (sender.username.equals(username));
    }

    public void sendMessage(Message msg) {
        if (!userIsAuthorizedToSend(msg)) {
            throw new InsufficientAuthenticationException("Not Authorized to send this message!");
        }

        messageRepository.insert(msg);
    }

    public void sendMessage(String recipientName, String messageText) {
        if (!AuthHelper.isLoggedIn()) {
            throw new InsufficientAuthenticationException("Not Authorized to send this message!");
        }
        else if (!isValidMessageText(messageText)) {
            throw new IllegalArgumentException("messageText should not be empty!");
        }

        Message msg = new Message();
        msg.recipient_id = userRepository.getByUsername(recipientName).id;
        msg.sender_id = userRepository.getByUsername(AuthHelper.getUserName()).id;
        msg.message = messageText;

        messageRepository.insert(msg);
    }

    public boolean isValidMessageText(String messageText) {
        return !messageText.isEmpty();
    }

    public class Conversation {
        public List<Message> conversation;
        public MyUser currentUser;
        public MyUser contact;
    }
    public Conversation getConversation(String contactName, int limit, int offset) {
        if (!AuthHelper.isLoggedIn()) {
            throw new InsufficientAuthenticationException("User not logged in!");
        }

        if (limit > maxLimit) throw new IllegalArgumentException("Limit must less than: " + maxLimit);

        Conversation response = new Conversation();
        response.contact = userRepository.getByUsername(contactName);
        response.currentUser = userRepository.getByUsername(AuthHelper.getUserName());
        response.conversation = messageRepository.getConversation(response.currentUser.id, response.contact.id, limit, offset);
        
        return response;
    }

}

