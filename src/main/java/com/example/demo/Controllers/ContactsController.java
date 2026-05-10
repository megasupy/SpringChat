package com.example.demo.Controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import com.example.demo.Services.ContactsService;

import com.example.demo.Models.MyUser;

@RestController
public class ContactsController {
    private final ContactsService service;

    public ContactsController(ContactsService service) {
        this.service = service;
    }

    @GetMapping("/contacts")
    public String myContacts() {
        StringBuilder html = new StringBuilder();

        List<MyUser> contacts = service.getMyContacts();
        for (var contact : contacts) {
            String contactNameEscaped = HtmlUtils.htmlEscape(contact.username);
            html.append("<li><a href=\"/chat?contact_name=").append(contactNameEscaped)
                .append("\" >").append(contactNameEscaped).append("</a></li>");
        }
        return html.toString();
    }
}

