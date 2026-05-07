package com.example.demo;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.UUID;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

class MyUser {
    UUID id;
    String username;
    String password_hashed;
    Timestamp created_at;
}

@Repository
class UserRepository {
    @Autowired
    JdbcTemplate template;

    public List<MyUser> getAll() {
        String sql = "SELECT * FROM users;";
        List<MyUser> result = template.query(sql, (ResultSet rs, int rowNum) -> {
            MyUser m = new MyUser();
            m.id = rs.getObject("id", UUID.class);
            m.username = rs.getString("username");
            m.password_hashed = rs.getString("password_hashed");
            m.created_at = rs.getTimestamp("created_at");
            return m;
        });
        return result;
    }

    public MyUser get(UUID id) {
        String sql = "SELECT * FROM users WHERE id = ?;";
        MyUser result = template.queryForObject(sql, (ResultSet rs, int rowNum) -> {
            MyUser m = new MyUser();
            m.id = rs.getObject("id", UUID.class);
            m.username = rs.getString("username");
            m.password_hashed = rs.getString("password_hashed");
            m.created_at = rs.getTimestamp("created_at");
            return m;
        }, id); 
        return result;
    }

    public MyUser getByUsername(String username) throws DataAccessException {
        String sql = "SELECT * FROM users WHERE username = ?;";
        MyUser result = template.queryForObject(sql, (ResultSet rs, int rowNum) -> {
            MyUser m = new MyUser();
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


