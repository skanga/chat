package com.example.llm;

import java.io.IOException;

public class Llm {

    private final ModelRegistry modelRegistry;
    private final AliasManager aliasManager;
    private final LogManager logManager;
    private final ConversationManager conversationManager;

    public Llm() {
        this(new ModelRegistry(new ModelConfig()), new AliasManager());
    }

    public Llm(ModelRegistry modelRegistry, AliasManager aliasManager) {
        this.modelRegistry = modelRegistry;
        this.aliasManager = aliasManager;
        
        ModelConfig modelConfig = modelRegistry.getConfig();
        String logType = modelConfig.get("log.type").orElse("none");
        String logPath = modelConfig.get("log.path").orElse(System.getProperty("user.home") + "/.llm/logs/conversations");
        
        if (!"none".equalsIgnoreCase(logType)) {
            this.logManager = new LogManager(logType, logPath);
            this.conversationManager = new ConversationManager(logManager, this);
        } else {
            this.logManager = null;
            this.conversationManager = null;
        }
    }

    public LlmChatModel getModel(String modelName) throws IOException {
        String resolvedModelName = aliasManager.resolveAlias(modelName);
        String[] parts = resolvedModelName.split("/", 2);
        if (parts.length == 2) {
            return modelRegistry.getModel(parts[0], parts[1]);
        } else {
            return modelRegistry.getModel(resolvedModelName);
        }
    }

    public LlmEmbeddingModel getEmbeddingModel(String modelName) throws IOException {
        String resolvedModelName = aliasManager.resolveAlias(modelName);
        String[] parts = resolvedModelName.split(":", 2);
        if (parts.length == 2) {
            return modelRegistry.getEmbeddingModel(parts[0], parts[1]);
        } else {
            return modelRegistry.getEmbeddingModel(resolvedModelName);
        }
    }

    public AliasManager getAliasManager() {
        return aliasManager;
    }

    public LogManager getLogManager() {
        return logManager;
    }

    public ConversationManager getConversationManager() {
        return conversationManager;
    }

    public boolean isLoggingEnabled() {
        return logManager != null;
    }

    public boolean isConversationEnabled() {
        return conversationManager != null;
    }
}
