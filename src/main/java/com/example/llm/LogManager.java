package com.example.llm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.Properties;

public class LogManager {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String logType;
    private final String logPath;
    private Connection dbConnection;

    public LogManager(String logType, String logPath) {
        this.logType = logType;
        this.logPath = logPath;
        
        if ("h2".equalsIgnoreCase(logType)) {
            initializeDatabase();
        } else if ("jsonl".equalsIgnoreCase(logType)) {
            ensureJsonlFileExists();
        }
    }

    private void initializeDatabase() {
        try {
            // Explicitly register H2 driver
            Class.forName("org.h2.Driver");
            
            String dbPath = logPath.endsWith(".db") ? logPath : logPath + ".db";
            String jdbcUrl = "jdbc:h2:file:" + dbPath + ";AUTO_SERVER=TRUE";
            
            Properties dbProps = new Properties();
            dbProps.setProperty("user", "sa");
            dbProps.setProperty("password", "");
            
            dbConnection = DriverManager.getConnection(jdbcUrl, dbProps);
            
            // Create tables if they don't exist
            String createConversationsTableSQL = """
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
            
            String createMessagesTableSQL = """
                CREATE TABLE IF NOT EXISTS messages (
                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                    conversation_id BIGINT NOT NULL,
                    role VARCHAR(20) NOT NULL,
                    content TEXT NOT NULL,
                    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (conversation_id) REFERENCES conversations(id)
                )
            """;
            
            try (Statement connectionStatement = dbConnection.createStatement()) {
                connectionStatement.execute(createConversationsTableSQL);
                connectionStatement.execute(createMessagesTableSQL);
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

    public String startConversation(String model, String initialPrompt, String response, 
                                  Integer promptTokens, Integer responseTokens, Integer totalTokens,
                                  Long durationMs, String schema, List<Object> tools, Map<String, Object> metadata) {
        
        String conversationId = UUID.randomUUID().toString();
        
        if ("h2".equalsIgnoreCase(logType)) {
            return startConversationInH2(conversationId, model, initialPrompt, response, promptTokens, responseTokens, totalTokens, durationMs, schema, tools, metadata);
        } else {
            return startConversationInJsonl(conversationId, model, initialPrompt, response, promptTokens, responseTokens, totalTokens, durationMs, schema, tools, metadata);
        }
    }

    private String startConversationInH2(String conversationId, String model, String initialPrompt, String response, 
                                       Integer promptTokens, Integer responseTokens, Integer totalTokens,
                                       Long durationMs, String schema, List<Object> tools, Map<String, Object> metadata) {
        
        try {
            // Insert conversation
            String conversationSQL = """
                INSERT INTO conversations (id, model, prompt, response, prompt_tokens, response_tokens, 
                                         total_tokens, duration_ms, schema, tools, metadata)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
            
            try (PreparedStatement pstmt = dbConnection.prepareStatement(conversationSQL)) {
                pstmt.setString(1, conversationId);
                pstmt.setString(2, model);
                pstmt.setString(3, initialPrompt);
                pstmt.setString(4, response);
                setNullableInt(pstmt, 5, promptTokens);
                setNullableInt(pstmt, 6, responseTokens);
                setNullableInt(pstmt, 7, totalTokens);
                setNullableLong(pstmt, 8, durationMs);
                pstmt.setString(9, schema);
                pstmt.setString(10, tools != null ? objectMapper.writeValueAsString(tools) : null);
                pstmt.setString(11, metadata != null ? objectMapper.writeValueAsString(metadata) : null);
                pstmt.executeUpdate();
            }
            
            // Insert messages
            String messageSQL = "INSERT INTO messages (conversation_id, role, content) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = dbConnection.prepareStatement(messageSQL)) {
                pstmt.setString(1, conversationId);
                pstmt.setString(2, "user");
                pstmt.setString(3, initialPrompt);
                pstmt.executeUpdate();
                
                pstmt.setString(2, "assistant");
                pstmt.setString(3, response);
                pstmt.executeUpdate();
            }
            
            return conversationId;
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Failed to start conversation in H2", e);
        }
    }

    private String startConversationInJsonl(String conversationId, String model, String initialPrompt, String response, 
                                        Integer promptTokens, Integer responseTokens, Integer totalTokens,
                                        Long durationMs, String schema, List<Object> tools, Map<String, Object> metadata) {
        
        try (FileWriter fw = new FileWriter(logPath, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            
            ObjectNode conversationEntry = objectMapper.createObjectNode();
            conversationEntry.put("id", conversationId);
            conversationEntry.put("timestamp", Instant.now().toString());
            conversationEntry.put("model", model);
            conversationEntry.put("type", "conversation");
            
            ArrayNode messages = objectMapper.createArrayNode();
            
            ObjectNode userMessage = objectMapper.createObjectNode();
            userMessage.put("role", "user");
            userMessage.put("content", initialPrompt);
            messages.add(userMessage);
            
            ObjectNode assistantMessage = objectMapper.createObjectNode();
            assistantMessage.put("role", "assistant");
            assistantMessage.put("content", response);
            messages.add(assistantMessage);
            
            conversationEntry.set("messages", messages);
            
            if (promptTokens != null) conversationEntry.put("prompt_tokens", promptTokens);
            if (responseTokens != null) conversationEntry.put("response_tokens", responseTokens);
            if (totalTokens != null) conversationEntry.put("total_tokens", totalTokens);
            if (durationMs != null) conversationEntry.put("duration_ms", durationMs);
            if (schema != null) conversationEntry.put("schema", schema);
            if (tools != null) conversationEntry.set("tools", objectMapper.valueToTree(tools));
            if (metadata != null) conversationEntry.set("metadata", objectMapper.valueToTree(metadata));
            
            out.println(objectMapper.writeValueAsString(conversationEntry));
            return conversationId;
        } catch (IOException e) {
            throw new RuntimeException("Failed to start conversation in JSONL", e);
        }
    }

    public void continueConversation(String conversationId, String newPrompt, String newResponse, 
                                 Integer promptTokens, Integer responseTokens, Integer totalTokens,
                                 Long durationMs) {
        
        if ("h2".equalsIgnoreCase(logType)) {
            continueConversationInH2(conversationId, newPrompt, newResponse, promptTokens, responseTokens, totalTokens, durationMs);
        } else {
            continueConversationInJsonl(conversationId, newPrompt, newResponse, promptTokens, responseTokens, totalTokens, durationMs);
        }
    }

    private void continueConversationInH2(String conversationId, String newPrompt, String newResponse, 
                                      Integer promptTokens, Integer responseTokens, Integer totalTokens,
                                      Long durationMs) {
        
        try {
            String messageSQL = "INSERT INTO messages (conversation_id, role, content) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = dbConnection.prepareStatement(messageSQL)) {
                pstmt.setString(1, conversationId);
                pstmt.setString(2, "user");
                pstmt.setString(3, newPrompt);
                pstmt.executeUpdate();
                
                pstmt.setString(2, "assistant");
                pstmt.setString(3, newResponse);
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to continue conversation in H2", e);
        }
    }

    private void continueConversationInJsonl(String conversationId, String newPrompt, String newResponse, 
                                           Integer promptTokens, Integer responseTokens, Integer totalTokens,
                                           Long durationMs) {
        
        try (FileWriter fw = new FileWriter(logPath, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            
            ObjectNode messageEntry = objectMapper.createObjectNode();
            messageEntry.put("conversation_id", conversationId);
            messageEntry.put("timestamp", Instant.now().toString());
            messageEntry.put("type", "message");
            
            ArrayNode messages = objectMapper.createArrayNode();
            
            ObjectNode userMessage = objectMapper.createObjectNode();
            userMessage.put("role", "user");
            userMessage.put("content", newPrompt);
            messages.add(userMessage);
            
            ObjectNode assistantMessage = objectMapper.createObjectNode();
            assistantMessage.put("role", "assistant");
            assistantMessage.put("content", newResponse);
            messages.add(assistantMessage);
            
            messageEntry.set("messages", messages);
            
            if (promptTokens != null) messageEntry.put("prompt_tokens", promptTokens);
            if (responseTokens != null) messageEntry.put("response_tokens", responseTokens);
            if (totalTokens != null) messageEntry.put("total_tokens", totalTokens);
            if (durationMs != null) messageEntry.put("duration_ms", durationMs);
            
            out.println(objectMapper.writeValueAsString(messageEntry));
        } catch (IOException e) {
            throw new RuntimeException("Failed to continue conversation in JSONL", e);
        }
    }

    public List<String> getConversationHistory(String conversationId, int limit) {
        if ("h2".equalsIgnoreCase(logType)) {
            return getConversationHistoryFromH2(conversationId, limit);
        } else {
            return getConversationHistoryFromJsonl(conversationId, limit);
        }
    }

    private List<String> getConversationHistoryFromH2(String conversationId, int limit) {
        List<String> history = new ArrayList<>();
        
        String sql = """
            SELECT role, content FROM messages 
            WHERE conversation_id = ? 
            ORDER BY timestamp ASC 
            LIMIT ?
        """;
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setString(1, conversationId);
            pstmt.setInt(2, limit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String role = rs.getString("role");
                    String content = rs.getString("content");
                    history.add(role + ": " + content);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get conversation history from H2", e);
        }
        
        return history;
    }

    private List<String> getConversationHistoryFromJsonl(String conversationId, int limit) {
        List<String> history = new ArrayList<>();
        
        try {
            List<String> lines = Files.readAllLines(Paths.get(logPath));
            List<String> conversationLines = new ArrayList<>();
            
            // Find all lines for this conversation
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    ObjectNode node = objectMapper.readValue(line, ObjectNode.class);
                    if (node.has("conversation_id") && node.get("conversation_id").asText().equals(conversationId)) {
                        conversationLines.add(line);
                    }
                }
            }
            
            // Process the conversation
            for (String line : conversationLines) {
                ObjectNode node = objectMapper.readValue(line, ObjectNode.class);
                if (node.has("messages")) {
                    for (Object message : objectMapper.convertValue(node.get("messages"), new TypeReference<List<Map<String, String>>>() {})) {
                        Map<String, String> msg = (Map<String, String>) message;
                        history.add(msg.get("role") + ": " + msg.get("content"));
                    }
                }
            }
            
            // Limit the history
            return history.size() > limit ? history.subList(history.size() - limit, history.size()) : history;
        } catch (IOException e) {
            throw new RuntimeException("Failed to get conversation history from JSONL", e);
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
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
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
                    
                    Conversation conv = new Conversation();
                    
                    // Handle both new format (with type=conversation) and legacy format
                    if (node.has("type") && "conversation".equals(node.get("type").asText())) {
                        conv.setId(node.get("id").asLong());
                        conv.setTimestamp(Instant.parse(node.get("timestamp").asText()));
                        conv.setModel(node.get("model").asText());
                        conv.setPrompt("Conversation started");
                        conv.setResponse("Conversation initialized");
                        
                        if (node.has("prompt_tokens")) conv.setPromptTokens(node.get("prompt_tokens").asInt());
                        if (node.has("response_tokens")) conv.setResponseTokens(node.get("response_tokens").asInt());
                        if (node.has("total_tokens")) conv.setTotalTokens(node.get("total_tokens").asInt());
                        if (node.has("duration_ms")) conv.setDurationMs(node.get("duration_ms").asLong());
                        if (node.has("schema")) conv.setSchema(node.get("schema").asText());
                    } else if (node.has("model") && node.has("prompt") && node.has("response")) {
                        // Legacy format from logConversation
                        conv.setId(0); // Legacy entries don't have IDs
                        conv.setTimestamp(Instant.parse(node.get("timestamp").asText()));
                        conv.setModel(node.get("model").asText());
                        conv.setPrompt(node.get("prompt").asText());
                        conv.setResponse(node.get("response").asText());
                        
                        if (node.has("prompt_tokens")) conv.setPromptTokens(node.get("prompt_tokens").asInt());
                        if (node.has("response_tokens")) conv.setResponseTokens(node.get("response_tokens").asInt());
                        if (node.has("total_tokens")) conv.setTotalTokens(node.get("total_tokens").asInt());
                        if (node.has("duration_ms")) conv.setDurationMs(node.get("duration_ms").asLong());
                        if (node.has("schema")) conv.setSchema(node.get("schema").asText());
                    } else {
                        continue; // Skip unrecognized formats
                    }
                    
                    conversations.add(conv);
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
        
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
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
                    if (node.has("type") && "conversation".equals(node.get("type").asText()) && 
                        node.get("id").asLong() == id) {
                        Conversation conv = new Conversation();
                        conv.setId(node.get("id").asLong());
                        conv.setTimestamp(Instant.parse(node.get("timestamp").asText()));
                        conv.setModel(node.get("model").asText());
                        conv.setPrompt("Conversation");
                        conv.setResponse("Details available");
                        return conv;
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

    // Backward compatibility method for legacy code
    public void logConversation(String model, String prompt, String response, 
                              Integer promptTokens, Integer responseTokens, Integer totalTokens,
                              Long durationMs, String schema, List<Object> tools, Map<String, Object> metadata) {
        if ("h2".equalsIgnoreCase(logType)) {
            logConversationToH2(model, prompt, response, promptTokens, responseTokens, totalTokens, durationMs, schema, tools, metadata);
        } else {
            logConversationToJsonl(model, prompt, response, promptTokens, responseTokens, totalTokens, durationMs, schema, tools, metadata);
        }
    }

    // Overloaded method for backward compatibility with nullable parameters
    public void logConversation(String model, String prompt, String response, 
                              Integer promptTokens, Integer responseTokens, Integer totalTokens,
                              Long durationMs, String schema, List<Object> tools, Object ignored) {
        logConversation(model, prompt, response, promptTokens, responseTokens, totalTokens, durationMs, schema, tools, null);
    }

    private void logConversationToJsonl(String model, String prompt, String response, 
                                    Integer promptTokens, Integer responseTokens, Integer totalTokens,
                                    Long durationMs, String schema, List<Object> tools, Map<String, Object> metadata) {
        
        try (FileWriter fw = new FileWriter(logPath, true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            
            ObjectNode entry = objectMapper.createObjectNode();
            entry.put("timestamp", Instant.now().toString());
            entry.put("model", model);
            entry.put("prompt", prompt);
            entry.put("response", response);
            
            if (promptTokens != null) entry.put("prompt_tokens", promptTokens);
            if (responseTokens != null) entry.put("response_tokens", responseTokens);
            if (totalTokens != null) entry.put("total_tokens", totalTokens);
            if (durationMs != null) entry.put("duration_ms", durationMs);
            if (schema != null) entry.put("schema", schema);
            if (tools != null) entry.set("tools", objectMapper.valueToTree(tools));
            if (metadata != null) entry.set("metadata", objectMapper.valueToTree(metadata));
            
            out.println(objectMapper.writeValueAsString(entry));
        } catch (IOException e) {
            throw new RuntimeException("Failed to log conversation to JSONL", e);
        }
    }

    private void logConversationToH2(String model, String prompt, String response, 
                                   Integer promptTokens, Integer responseTokens, Integer totalTokens,
                                   Long durationMs, String schema, List<Object> tools, Map<String, Object> metadata) {
        try {
            // Insert conversation without specifying ID (auto-generated)
            String conversationSQL = """
                INSERT INTO conversations (model, prompt, response, prompt_tokens, response_tokens, 
                                         total_tokens, duration_ms, schema, tools, metadata)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
            
            try (PreparedStatement pstmt = dbConnection.prepareStatement(conversationSQL, Statement.RETURN_GENERATED_KEYS)) {
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
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Failed to log conversation to H2", e);
        }
    }

    public void close() {
        if (dbConnection != null) {
            try {
                dbConnection.close();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to close H2 connection", e);
            }
        }
    }
}