package com.example.demo.Controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.demo.Services.MessageService;
import com.example.demo.AuthHelper;
import com.example.demo.Models.Message;


@RestController
public class MessageController {
    @Autowired
    MessageService service;

    @GetMapping ("/message")
    public ResponseEntity<String> getConversation(@RequestParam String contact_name, @RequestParam(defaultValue = "50") int limit, @RequestParam(defaultValue = "0") int offset) {
        if (limit > 100 || limit < 0) return ResponseEntity.badRequest().body("Limit must be positive and less than 100");
        MessageService.Conversation conversation = service.getConversation(contact_name, limit, offset);

        contact_name = HtmlUtils.htmlEscape(contact_name);
        String userName = HtmlUtils.htmlEscape(AuthHelper.getUserName());
        String contactSentPrefix = "<article><strong>" + contact_name + "</strong>: ";
        String userSentPrefix = "<article><strong>" + userName + "</strong>: ";
        String suffix = "</article>";

        StringBuilder html = new StringBuilder();
        for (var message : conversation.conversation) {
            if (conversation.currentUser.id.equals(message.sender_id)) {
                html.append(userSentPrefix);
            }
            else {
                html.append(contactSentPrefix);
            }

            html.append(HtmlUtils.htmlEscape(message.message))
                .append(suffix);
        }

        return ResponseEntity.ok(html.toString());
    }

    @PostMapping("/message/deprecated") 
    public ResponseEntity<String> sendMessage(@RequestBody Message msg) {
        service.sendMessage(msg);
        return ResponseEntity.status(HttpStatus.CREATED).body("Inserted \"" + msg.message + "\"");
    }

    @PostMapping("/message") 
    public ResponseEntity<String> sendMessage(@RequestParam String contact_name, @RequestParam String message_text) {
        if (!service.isValidMessageText(message_text)) {
            return ResponseEntity.badRequest().body("Error: Message is Empty!");
        }
        service.sendMessage(contact_name, message_text);
        return ResponseEntity.ok("Inserted Message");
    }
}
