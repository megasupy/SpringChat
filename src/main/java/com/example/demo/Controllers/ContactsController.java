package com.example.demo.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.Services.ContactsService;

import com.example.demo.Models.MyUser;

@RestController
public class ContactsController {
    @Autowired
    ContactsService service;

    @GetMapping("/contacts")
    public String myContacts() {
        String html = "";

        List<MyUser> contacts = service.getMyContacts();
        for (var contact : contacts) {
            html += 
                "<li hx-get=\"/chat?username=\"" + contact.username + "\" hx-trigger=\"click\">" 
                + contact.username + "</li>";
        }
        return html;
    }
}

