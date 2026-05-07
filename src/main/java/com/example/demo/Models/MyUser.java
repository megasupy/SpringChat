package com.example.demo.Models;

import java.util.UUID;
import java.sql.Timestamp;

public class MyUser {
    public UUID id;
    public String username;
    public String password_hashed;
    public Timestamp created_at;
}


