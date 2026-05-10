package com.example.demo.Repositories;

import java.util.UUID;
import java.util.List;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.RowMapper;
import com.example.demo.Models.MyUser;

@Repository
public class UserRepository {
    private final JdbcTemplate template;

    public UserRepository(JdbcTemplate template) {
        this.template = template;
    }

    final private RowMapper<MyUser> rowMapper = (rs, rowNum) -> {
            MyUser m = new MyUser();
            m.id = rs.getObject("id", UUID.class);
            m.username = rs.getString("username");
            m.password_hashed = rs.getString("password_hashed");
            m.created_at = rs.getTimestamp("created_at");
            return m;
    };
    public List<MyUser> getAll() {
        String sql = "SELECT * FROM users;";
        List<MyUser> result = template.query(sql, rowMapper);
        return result;
    }

    public MyUser get(UUID id) {
        String sql = "SELECT * FROM users WHERE id = ?;";
        MyUser result = template.queryForObject(sql, rowMapper, id); 
        return result;
    }

    public MyUser getByUsername(String username) throws DataAccessException {
        String sql = "SELECT * FROM users WHERE username = ?;";
        MyUser result = template.queryForObject(sql, rowMapper, username );
        return result;
    }

    public List<MyUser> getAllExcept(String username) throws DataAccessException {
        String sql = "SELECT * FROM users WHERE username != ?;";
        List<MyUser> result = template.query(sql, rowMapper, username);
        return result;
    }

    public List<MyUser> searchByUsername(String username_fragment) throws DataAccessException {
        String sql = "SELECT * FROM users WHERE username LIKE ?;";
        List<MyUser> result = template.query(sql, rowMapper, "%" + username_fragment + "%");
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


