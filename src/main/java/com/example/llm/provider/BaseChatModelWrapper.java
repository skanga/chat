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
    private final ChatModel chatModel;
    private final StreamingChatModel streamingModel;

    public BaseChatModelWrapper(ChatModel chatModel, StreamingChatModel streamingModel) {
        this.chatModel = chatModel;
        this.streamingModel = streamingModel;
    }

    @Override
    public LlmResponse chat(LlmRequest llmRequest) {
        UserMessage userMessage = UserMessage.from(llmRequest.prompt());
        try {
            ChatResponse response = chatModel.chat(userMessage);
            return new LlmResponse(response.aiMessage().text());
        } catch (Exception e) {
            throw new RuntimeException("Error calling API: " + e.getMessage(), e);
        }
    }

    @Override
    public void streamChat(LlmRequest llmRequest, StreamingChatResponseHandler responseHandler) {
        try {
            streamingModel.chat(llmRequest.prompt(), responseHandler);
        } catch (Exception e) {
            throw new RuntimeException("Error calling API: " + e.getMessage(), e);
        }
    }
}
