package com.example.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class LogManager {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String logType;
    private final String logPath;
    private Connection connection;

    public LogManager(String logType, String logPath) {
        this.logType = logType;
        this.logPath = logPath;
        
        if ("h2".equalsIgnoreCase(logType)) {
            initializeH2Database();
        } else if ("jsonl".equalsIgnoreCase(logType)) {
            ensureJsonlFileExists();
        }
    }

    private void initializeH2Database() {
        try {
            // Explicitly register H2 driver
            Class.forName("org.h2.Driver");
            
            String dbPath = logPath.endsWith(".db") ? logPath : logPath + ".db";
            String jdbcUrl = "jdbc:h2:file:" + dbPath + ";AUTO_SERVER=TRUE";
            
            Properties props = new Properties();
            props.setProperty("user", "sa");
            props.setProperty("password", "");
            
            connection = DriverManager.getConnection(jdbcUrl, props);
            
            // Create table if it doesn't exist
            String createTableSQL = """
                CREATE TABLE IF NOT EXISTS conversations (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    model VARCHAR(255) NOT NULL,
                    prompt TEXT NOT NULL,
                    response TEXT NOT NULL,
                    prompt_tokens INTEGER,
                    response_tokens INTEGER,
                    total_tokens INTEGER,
                    duration_ms BIGINT,
                    schema VARCHAR(500),
                    tools TEXT,
                    metadata JSON
                )
            """;
            
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createTableSQL);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("H2 JDBC driver not found. Please ensure H2 dependency is included.", e);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize H2 database", e);
        }
    }

    private void ensureJsonlFileExists() {
        try {
            Path path = Paths.get(logPath);
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            if (!Files.exists(path)) {
                Files.createFile(path);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create JSONL log file", e);
        }
    }

    public void logConversation(String model, String prompt, String response, 
                              Integer promptTokens, Integer responseTokens, Integer totalTokens,
                              Long durationMs, String schema, List<Object> tools, Map<String, Object> metadata) {
        
        if ("h2".equalsIgnoreCase(logType)) {
            logToH2(model, prompt, response, promptTokens, responseTokens, totalTokens, 
                   durationMs, schema, tools, metadata);
        } else {
            logToJsonl(model, prompt, response, promptTokens, responseTokens, totalTokens,
                      durationMs, schema, tools, metadata);
        }
    }

    private void logToH2(String model, String prompt, String response, 
                        Integer promptTokens, Integer responseTokens, Integer totalTokens,
                        Long durationMs, String schema, List<Object> tools, Map<String, Object> metadata) {
        
        String sql = """
            INSERT INTO conversations (model, prompt, response, prompt_tokens, response_tokens, 
                                       total_tokens, duration_ms, schema, tools, metadata)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, model);
            pstmt.setString(2, prompt);
            pstmt.setString(3, response);
            setNullableInt(pstmt, 4, promptTokens);
            setNullableInt(pstmt, 5, responseTokens);
            setNullableInt(pstmt, 6, totalTokens);
            setNullableLong(pstmt, 7, durationMs);
            pstmt.setString(8, schema);
            pstmt.setString(9, tools != null ? objectMapper.writeValueAsString(tools) : null);
            pstmt.setString(10, metadata != null ? objectMapper.writeValueAsString(metadata) : null);
            
            pstmt.executeUpdate();
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Failed to log to H2 database", e);
        }
    }

    private void logToJsonl(String model, String prompt, String response, 
                           Integer promptTokens, Integer responseTokens, Integer totalTokens,
                           Long durationMs, String schema, List<Object> tools, Map<String, Object> metadata) {
        
        try (FileWriter fw = new FileWriter(logPath, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            
            ObjectNode logEntry = objectMapper.createObjectNode();
            logEntry.put("id", System.currentTimeMillis());
            logEntry.put("timestamp", Instant.now().toString());
            logEntry.put("model", model);
            logEntry.put("prompt", prompt);
            logEntry.put("response", response);
            
            if (promptTokens != null) logEntry.put("prompt_tokens", promptTokens);
            if (responseTokens != null) logEntry.put("response_tokens", responseTokens);
            if (totalTokens != null) logEntry.put("total_tokens", totalTokens);
            if (durationMs != null) logEntry.put("duration_ms", durationMs);
            if (schema != null) logEntry.put("schema", schema);
            if (tools != null) {
                logEntry.set("tools", objectMapper.valueToTree(tools));
            }
            if (metadata != null) {
                logEntry.set("metadata", objectMapper.valueToTree(metadata));
            }
            
            out.println(objectMapper.writeValueAsString(logEntry));
        } catch (IOException e) {
            throw new RuntimeException("Failed to log to JSONL file", e);
        }
    }

    public List<Conversation> getConversations(int limit, int offset) {
        if ("h2".equalsIgnoreCase(logType)) {
            return getConversationsFromH2(limit, offset);
        } else {
            return getConversationsFromJsonl(limit, offset);
        }
    }

    private List<Conversation> getConversationsFromH2(int limit, int offset) {
        List<Conversation> conversations = new ArrayList<>();
        String sql = """
            SELECT id, timestamp, model, prompt, response, prompt_tokens, response_tokens, 
                   total_tokens, duration_ms, schema, tools, metadata
            FROM conversations
            ORDER BY timestamp DESC
            LIMIT ? OFFSET ?
        """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            pstmt.setInt(2, offset);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    conversations.add(mapResultSetToConversation(rs));
                }
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Failed to retrieve conversations from H2", e);
        }
        
        return conversations;
    }

    private List<Conversation> getConversationsFromJsonl(int limit, int offset) {
        List<Conversation> conversations = new ArrayList<>();
        
        try {
            List<String> lines = Files.readAllLines(Paths.get(logPath));
            int startIndex = Math.max(0, lines.size() - offset - limit);
            int endIndex = Math.max(0, lines.size() - offset);
            
            for (int i = startIndex; i < endIndex; i++) {
                String line = lines.get(i);
                if (!line.trim().isEmpty()) {
                    ObjectNode node = objectMapper.readValue(line, ObjectNode.class);
                    conversations.add(mapJsonNodeToConversation(node));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read conversations from JSONL", e);
        }
        
        return conversations;
    }

    public Conversation getConversation(long id) {
        if ("h2".equalsIgnoreCase(logType)) {
            return getConversationFromH2(id);
        } else {
            return getConversationFromJsonl(id);
        }
    }

    private Conversation getConversationFromH2(long id) {
        String sql = """
            SELECT id, timestamp, model, prompt, response, prompt_tokens, response_tokens, 
                   total_tokens, duration_ms, schema, tools, metadata
            FROM conversations
            WHERE id = ?
        """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToConversation(rs);
                }
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Failed to retrieve conversation from H2", e);
        }
        
        return null;
    }

    private Conversation getConversationFromJsonl(long id) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(logPath));
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    ObjectNode node = objectMapper.readValue(line, ObjectNode.class);
                    if (node.get("id").asLong() == id) {
                        return mapJsonNodeToConversation(node);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read conversation from JSONL", e);
        }
        
        return null;
    }

    private Conversation mapResultSetToConversation(ResultSet rs) throws SQLException, IOException {
        Conversation conv = new Conversation();
        conv.setId(rs.getLong("id"));
        conv.setTimestamp(rs.getTimestamp("timestamp").toInstant());
        conv.setModel(rs.getString("model"));
        conv.setPrompt(rs.getString("prompt"));
        conv.setResponse(rs.getString("response"));
        conv.setPromptTokens(getNullableInt(rs, "prompt_tokens"));
        conv.setResponseTokens(getNullableInt(rs, "response_tokens"));
        conv.setTotalTokens(getNullableInt(rs, "total_tokens"));
        conv.setDurationMs(getNullableLong(rs, "duration_ms"));
        conv.setSchema(rs.getString("schema"));
        
        String toolsJson = rs.getString("tools");
            if (toolsJson != null) {
                conv.setTools(objectMapper.readValue(toolsJson, new TypeReference<List<Object>>() {}));
            }
            
            String metadataJson = rs.getString("metadata");
            if (metadataJson != null) {
                conv.setMetadata(objectMapper.readValue(metadataJson, new TypeReference<Map<String, Object>>() {}));
            }
        
        return conv;
    }

    private Conversation mapJsonNodeToConversation(ObjectNode node) throws IOException {
        Conversation conv = new Conversation();
        conv.setId(node.get("id").asLong());
        conv.setTimestamp(Instant.parse(node.get("timestamp").asText()));
        conv.setModel(node.get("model").asText());
        conv.setPrompt(node.get("prompt").asText());
        conv.setResponse(node.get("response").asText());
        
        if (node.has("prompt_tokens")) conv.setPromptTokens(node.get("prompt_tokens").asInt());
        if (node.has("response_tokens")) conv.setResponseTokens(node.get("response_tokens").asInt());
        if (node.has("total_tokens")) conv.setTotalTokens(node.get("total_tokens").asInt());
        if (node.has("duration_ms")) conv.setDurationMs(node.get("duration_ms").asLong());
        if (node.has("schema")) conv.setSchema(node.get("schema").asText());
        if (node.has("tools")) {
            conv.setTools(objectMapper.convertValue(node.get("tools"), new TypeReference<List<Object>>() {}));
        }
        if (node.has("metadata")) {
            conv.setMetadata(objectMapper.convertValue(node.get("metadata"), new TypeReference<Map<String, Object>>() {}));
        }
        
        return conv;
    }

    private void setNullableInt(PreparedStatement pstmt, int index, Integer value) throws SQLException {
        if (value != null) {
            pstmt.setInt(index, value);
        } else {
            pstmt.setNull(index, Types.INTEGER);
        }
    }

    private void setNullableLong(PreparedStatement pstmt, int index, Long value) throws SQLException {
        if (value != null) {
            pstmt.setLong(index, value);
        } else {
            pstmt.setNull(index, Types.BIGINT);
        }
    }

    private Integer getNullableInt(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        return rs.wasNull() ? null : value;
    }

    private Long getNullableLong(ResultSet rs, String columnName) throws SQLException {
        long value = rs.getLong(columnName);
        return rs.wasNull() ? null : value;
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to close H2 connection", e);
            }
        }
    }
}