package com.example.demo;

import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

class Message {
    public UUID sender_id;
    public String messageText;
    public UUID recipient_id;
}

@RestController
public class MessageController {
    @Autowired
    JdbcTemplate template;

    @GetMapping("/users/gettwo")
    public List<UUID> Get2UserIDs() {
        String sql = "SELECT id FROM users LIMIT 2;";
        return template.query(sql, (ResultSet rs, int rowNum) -> {
            return rs.getObject("id", UUID.class);
        }
        );
    }

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
        String sql = "SELECT sender_id, message, recipient_id FROM messages;";
        List<Message> result = template.query(sql, (ResultSet rs, int rowNum) -> {
            Message m = new Message();
            m.sender_id = rs.getObject("sender_id", UUID.class);
            m.recipient_id = rs.getObject("recipient_id", UUID.class);
            m.messageText = rs.getObject("message", String.class);
            return m;
        });
        return ResponseEntity.ok().body(result);

    }

    @PostMapping("/message") 
    public ResponseEntity<String> sendMessage(@RequestBody Message msg) {
        String sql = "INSERT INTO messages (sender_id, recipient_id, message) VALUES (?, ?, ?);";
        template.update(sql, new Object[]{msg.sender_id, msg.recipient_id, msg.messageText});
        return ResponseEntity.status(HttpStatus.CREATED).body("Inserted \"" + msg.messageText + "\"");
    }

    @PostMapping("/message-test") 
    /*
    Test with:
    cmd:
    curl --json '{"messageText":"Hey All!"}' localhost:8080/message-test
    result:
    Inserted Hey All!
    */
    public ResponseEntity<String> sendMessageTest(@RequestBody Message msg) {
        List<UUID> userIDs = Get2UserIDs();
        if (userIDs.size() < 2) return ResponseEntity.internalServerError().body("Could not find two User IDs");
        String sql = "INSERT INTO messages (sender_id, recipient_id, message) VALUES (?, ?, ?);";
        template.update(sql, new Object[]{userIDs.get(0), userIDs.get(1), msg.messageText});
        return ResponseEntity.status(HttpStatus.CREATED).body("Inserted \"" + msg.messageText + "\"");
    }
    
}
