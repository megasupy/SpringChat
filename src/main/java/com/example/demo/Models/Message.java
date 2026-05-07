package com.example.demo.Models;

import java.util.UUID;
import java.sql.Timestamp;

public class Message {
    public UUID id;
    public UUID sender_id;
    public String message;
    public UUID recipient_id;
    public Timestamp created_at;
}


