package com.example.demo.Controllers;

import org.springframework.ui.Model;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.AuthHelper;

@Controller
public class PageController {
    @GetMapping("/") 
    public String index(Model m) {
        m.addAttribute("loggedin", AuthHelper.isLoggedIn());
        return "index";
    }

    @GetMapping("/login") 
    public String login(Model m) {
        return "login";
    }

    @GetMapping("/signup")
    public String signup(Model m) {
        return "signup";
    }

    @GetMapping("/chat") 
    public String chat(Model m, @RequestParam String userName) {
        m.addAttribute("dummy", userName);
        return "chat";
    }
}
