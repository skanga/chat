package com.example.llm;

import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;

public interface LlmChatModel {
    LlmResponse chat(LlmRequest llmRequest);
    void streamChat(LlmRequest llmRequest, StreamingChatResponseHandler handler);
}
