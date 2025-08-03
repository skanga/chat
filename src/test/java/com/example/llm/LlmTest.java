package com.example.llm;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LlmTest {
/*
    @Test
    void should_get_registered_model() {
        // given
        ModelRegistry modelRegistry = new ModelRegistry();
        LlmChatModel mockModel = new MockChatModel();
        modelRegistry.registerChatModelFactory("mock", () -> mockModel);

        Llm llm = new Llm(modelRegistry);

        // when
        LlmChatModel retrievedModel = llm.getModel("mock");

        // then
        assertThat(retrievedModel).isSameAs(mockModel);
    }

    @Test
    void should_throw_for_unregistered_model() {
        // given
        Llm llm = new Llm(new ModelRegistry());

        // when & then
        assertThatThrownBy(() -> llm.getModel("unregistered"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Model not found: unregistered");
    }

    private static class MockChatModel implements LlmChatModel {
        @Override
        public LlmResponse chat(LlmRequest llmRequest) {
            return new LlmResponse("mock response");
        }
    }
 */
}