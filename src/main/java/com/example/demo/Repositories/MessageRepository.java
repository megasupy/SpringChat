package com.example.demo.Repositories;

import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import com.example.demo.Models.Message;

@Repository
public class MessageRepository {
    private final JdbcTemplate template;

    public MessageRepository(JdbcTemplate template) {
        this.template = template;
    }

    RowMapper<Message> rowMapper = (rs, rowNum) -> {
        Message m = new Message();
        m.id = rs.getObject("id", UUID.class);
        m.sender_id = rs.getObject("sender_id", UUID.class);
        m.recipient_id = rs.getObject("recipient_id", UUID.class);
        m.message = rs.getString("message");
        m.created_at = rs.getTimestamp("created_at");
        return m;
    };

    public List<Message> getAll() {
        String sql = "SELECT id, sender_id, message, recipient_id, created_at FROM messages;";
        List<Message> result = template.query(sql, rowMapper);
        return result;
    }

    public Message get(UUID id) {
        String sql = "SELECT id, sender_id, message, recipient_id, created_at FROM messages WHERE id = ?;";
        Message result = template.queryForObject(sql, rowMapper, new Object[]{
                id,
            });

        return result;
    }

    public List<Message> getConversation(UUID userId1, UUID userId2, int limit, int offset) {
        String sql = """
            SELECT * FROM messages
                WHERE (sender_id = ? AND recipient_id = ?)
                OR (recipient_id = ? AND sender_id = ?)
                ORDER BY messages.created_at ASC 
                LIMIT ? OFFSET ?;
            """;
        List<Message> result = template.query(sql, rowMapper, 
            userId1, userId2, 
                    userId1, userId2, // it's important you don't swap these!
                    limit, offset);
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
