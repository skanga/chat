package com.example.llm.provider;

import com.example.llm.LlmChatModel;
import com.example.llm.LlmRequest;
import com.example.llm.LlmResponse;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;

public abstract class BaseChatModelWrapper implements LlmChatModel {

    private final ChatModel model;
    private final StreamingChatModel streamingModel;

    public BaseChatModelWrapper(ChatModel model, StreamingChatModel streamingModel) {
        this.model = model;
        this.streamingModel = streamingModel;
    }

    @Override
    public LlmResponse chat(LlmRequest llmRequest) {
        UserMessage userMessage = UserMessage.from(llmRequest.prompt());
        try {
            ChatResponse response = model.chat(userMessage);
            return new LlmResponse(response.aiMessage().text());
        } catch (Exception e) {
            throw new RuntimeException("Error calling API: " + e.getMessage(), e);
        }
    }

    @Override
    public void streamChat(LlmRequest llmRequest, StreamingChatResponseHandler handler) {
        try {
            streamingModel.chat(llmRequest.prompt(), handler);
        } catch (Exception e) {
            throw new RuntimeException("Error calling API: " + e.getMessage(), e);
        }
    }
}
