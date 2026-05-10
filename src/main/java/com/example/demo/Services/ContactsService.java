package com.example.demo.Services;

import org.springframework.stereotype.Service;

import java.util.List;

import com.example.demo.Repositories.UserRepository;
import com.example.demo.Models.MyUser;
import com.example.demo.AuthHelper;

@Service
public class ContactsService {
    private final UserRepository userRepository;

    public ContactsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<MyUser> getMyContacts() {
        return userRepository.getAllExcept(AuthHelper.getUserName());
    }
}
