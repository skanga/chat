package com.example.llm;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class Conversation {
    private long id;
    private Instant timestamp;
    private String model;
    private String prompt;
    private String response;
    private Integer promptTokens;
    private Integer responseTokens;
    private Integer totalTokens;
    private Long durationMs;
    private String schema;
    private List<Object> tools;
    private Map<String, Object> metadata;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Integer getPromptTokens() {
        return promptTokens;
    }

    public void setPromptTokens(Integer promptTokens) {
        this.promptTokens = promptTokens;
    }

    public Integer getResponseTokens() {
        return responseTokens;
    }

    public void setResponseTokens(Integer responseTokens) {
        this.responseTokens = responseTokens;
    }

    public Integer getTotalTokens() {
        return totalTokens;
    }

    public void setTotalTokens(Integer totalTokens) {
        this.totalTokens = totalTokens;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public List<Object> getTools() {
        return tools;
    }

    public void setTools(List<Object> tools) {
        this.tools = tools;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "id=" + id +
                ", timestamp=" + timestamp +
                ", model='" + model + '\'' +
                ", prompt='" + prompt.substring(0, Math.min(prompt.length(), 50)) + "...'" +
                ", response='" + response.substring(0, Math.min(response.length(), 50)) + "...'" +
                ", promptTokens=" + promptTokens +
                ", responseTokens=" + responseTokens +
                ", totalTokens=" + totalTokens +
                ", durationMs=" + durationMs +
                '}';
    }
}