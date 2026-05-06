package com.example.demo;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.UUID;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

class User {
    UUID id;
    String username;
    String password_hashed;
    Timestamp created_at;
}

@Repository
class UserRepository {
    @Autowired
    JdbcTemplate template;

    public List<User> getAll() {
        String sql = "SELECT * FROM users;";
        List<User> result = template.query(sql, (ResultSet rs, int rowNum) -> {
            User m = new User();
            m.id = rs.getObject("id", UUID.class);
            m.username = rs.getString("username");
            m.password_hashed = rs.getString("password_hashed");
            m.created_at = rs.getTimestamp("created_at");
            return m;
        });
        return result;
    }

    public User get(UUID id) {
        String sql = "SELECT * FROM users WHERE id = ?;";
        User result = template.queryForObject(sql, (ResultSet rs, int rowNum) -> {
            User m = new User();
            m.id = rs.getObject("id", UUID.class);
            m.username = rs.getString("username");
            m.password_hashed = rs.getString("password_hashed");
            m.created_at = rs.getTimestamp("created_at");
            return m;
        }, id); 
        return result;
    }

    public User getByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?;";
        User result = template.queryForObject(sql, (ResultSet rs, int rowNum) -> {
            User m = new User();
            m.id = rs.getObject("id", UUID.class);
            m.username = rs.getString("username");
            m.password_hashed = rs.getString("password_hashed");
            m.created_at = rs.getTimestamp("created_at");
            return m;
        }, username );
        return result;
    }

    public boolean userNameExists(String username) {

        String sql = "SELECT EXISTS (SELECT 1 as username_exists FROM users WHERE username = ?);"; 
        return template.queryForObject(sql, Boolean.class, username);
    }

    public UUID insert(String username, String password_hashed) {
        String sql = """
            INSERT INTO users (username, password_hashed) 
            VALUES (?, ?) 
            RETURNING id;
        """;
        return template.queryForObject(sql, UUID.class, new Object[]{username, password_hashed});
    }
}


