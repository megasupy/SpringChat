package com.example.demo;

import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


@RestController
public class MessageController {
    @Autowired
    MessageRepository repository;

    @GetMapping("/hello")
    public String hello() {
        var str = "Hello World";
        return str;
    }

    /*
    test with this (consider piping into jq):
    curl localhost:8080/message
    */
    @GetMapping("/message")
    public ResponseEntity<List<Message>> getMessages() {
        List<Message> result = repository.getAll(); 
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/html/message")
    public String getMessagesHTML() {
        List<Message> result = repository.getAll();
        String html = "";
        for (Message m : result) {
            html += "<p>" + m.message + "</p>";
        }

        return html;

    }

    @PostMapping("/message") 
    public ResponseEntity<String> sendMessage(@RequestBody Message msg) {
        repository.insert(msg);
        return ResponseEntity.status(HttpStatus.CREATED).body("Inserted \"" + msg.message + "\"");
    }

    @PostMapping("/message-test") 
    /*
    Test with:
    cmd:
    curl --json '{"message":"Hey All!"}' localhost:8080/message-test
    result:
    Inserted Hey All!
    */
    public ResponseEntity<String> sendMessageTest(@RequestBody Message msg) {
        List<UUID> userIDs = repository.get2UserIDs();
        msg.sender_id = userIDs.get(0);
        msg.recipient_id = userIDs.get(1);
        repository.insert(msg);
        return ResponseEntity.status(HttpStatus.CREATED).body("Inserted \"" + msg.message + "\"");
    }
    
}
