package com.example.llm;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ConversationManager {
    private final LogManager logManager;

    public ConversationManager(LogManager logManager, Llm llm) {
        this.logManager = logManager;
    }

    public String startNewConversation(String model, String prompt, String response, 
                                   Integer promptTokens, Integer responseTokens, Integer totalTokens,
                                   Long durationMs, String schema, List<Object> tools, 
                                   Map<String, Object> metadata) {
        
        return logManager.startConversation(model, prompt, response, promptTokens, responseTokens, 
                                          totalTokens, durationMs, schema, tools, metadata);
    }

    public String continueConversation(String conversationId, String model, String newPrompt, String newResponse,
                                     Integer promptTokens, Integer responseTokens, Integer totalTokens,
                                     Long durationMs) {
        
        logManager.continueConversation(conversationId, newPrompt, newResponse, promptTokens, responseTokens, totalTokens, durationMs);
        return conversationId;
    }

    public List<String> getConversationContext(String conversationId, int maxMessages) {
        return logManager.getConversationHistory(conversationId, maxMessages);
    }

    public List<Conversation> getRecentConversations(int limit) {
        return logManager.getConversations(limit, 0);
    }

    public Conversation getConversation(String conversationId) {
        return logManager.getConversation(Long.parseLong(conversationId));
    }

    public String generateConversationId() {
        return UUID.randomUUID().toString();
    }

    public String buildContextPrompt(String conversationId, String newPrompt, int maxContextMessages) {
        List<String> history = getConversationContext(conversationId, maxContextMessages);
        
        if (history.isEmpty()) {
            return newPrompt;
        }
        
        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("Previous conversation:\n");
        
        for (String message : history) {
            contextBuilder.append(message).append("\n");
        }
        
        contextBuilder.append("\nNew prompt: ").append(newPrompt);
        
        return contextBuilder.toString();
    }
}