package com.example.demo.Repositories;

import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.example.demo.Models.Message;

@Repository
public class MessageRepository {
    @Autowired
    private JdbcTemplate template;

    public List<UUID> get2UserIDs() {
        String sql = "SELECT id FROM users LIMIT 2;";
        return template.query(sql, (ResultSet rs, int rowNum) -> {
            return rs.getObject("id", UUID.class);
        }
        );
    }

    public List<Message> getAll() {
        String sql = "SELECT id, sender_id, message, recipient_id, created_at FROM messages;";
        List<Message> result = template.query(sql, (ResultSet rs, int rowNum) -> {
            Message m = new Message();
            m.id = rs.getObject("id", UUID.class);
            m.sender_id = rs.getObject("sender_id", UUID.class);
            m.recipient_id = rs.getObject("recipient_id", UUID.class);
            m.message = rs.getString("message");
            m.created_at = rs.getTimestamp("created_at");
            return m;
        });
        return result;
    }

    public Message get(UUID id) {
        String sql = "SELECT id, sender_id, message, recipient_id, created_at FROM messages WHERE id = ?;";
        Message result = template.queryForObject(sql, (ResultSet rs, int rowNum) -> {
            Message m = new Message();
            m.id = rs.getObject("id", UUID.class);
            m.sender_id = rs.getObject("sender_id", UUID.class);
            m.recipient_id = rs.getObject("recipient_id", UUID.class);
            m.message = rs.getString("message");
            m.created_at = rs.getTimestamp("created_at");
            return m;
        }, new Object[]{
                id,
            });

        return result;
    }

    public void insert(Message msg) {
        String sql = """
        INSERT INTO messages (sender_id, recipient_id, message) 
        VALUES (?, ?, ?);
        """;
        template.update(sql, new Object[]{
            msg.sender_id, msg.recipient_id, msg.message
        });
        return; 
    }

    public void delete(UUID messageID) {
        String sql = """
        DELETE FROM messages WHERE id = ?;
        """;
        template.update(sql, new Object[]{
            messageID
        });
        return;
    }
}
