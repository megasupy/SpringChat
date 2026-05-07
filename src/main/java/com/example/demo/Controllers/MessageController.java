package com.example.demo.Controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;

import com.example.demo.Services.MessageService;
import com.example.demo.Models.Message;


@RestController
public class MessageController {
    @Autowired
    MessageService service;

    @PostMapping("/message") 
    public ResponseEntity<String> sendMessage(@RequestBody Message msg) {
        service.sendMessage(msg);
        return ResponseEntity.status(HttpStatus.CREATED).body("Inserted \"" + msg.message + "\"");
    }
}
