package com.example.demo.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.stereotype.Service;

import com.example.demo.Repositories.UserRepository;
import com.example.demo.Models.MyUser;
import com.example.demo.Repositories.MessageRepository;
import com.example.demo.Models.Message;
import com.example.demo.AuthHelper;

@Service
public class MessageService {
    @Autowired
    MessageRepository messageRepository;

    @Autowired
    UserRepository userRepository;

    private boolean userIsAuthorizedToSend(Message msg) {
        // might add a firends list or something later
        MyUser sender = userRepository.get(msg.sender_id);
        String username = AuthHelper.getUserName();
        return (sender.username == username);
    }

    public void sendMessage(Message msg) {
        if (!userIsAuthorizedToSend(msg)) {
            throw new InsufficientAuthenticationException("Not Authorized to send this message!");
        }

        messageRepository.insert(msg);
    }
}

