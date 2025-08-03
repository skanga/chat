package com.example.llm;

import java.util.List;

public record LlmRequest(String prompt, String schema, List<Object> tools) {
    public LlmRequest(String prompt) {
        this(prompt, null, null);
    }
    public LlmRequest(String prompt, String schema) {
        this(prompt, schema, null);
    }
}
