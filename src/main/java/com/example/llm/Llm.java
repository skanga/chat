package com.example.llm;

import java.io.IOException;

public class Llm {

    private final ModelRegistry modelRegistry;
    private final AliasManager aliasManager;
    private final LogManager logManager;

    public Llm() {
        this(new ModelRegistry(new Config()), new AliasManager());
    }

    public Llm(ModelRegistry modelRegistry, AliasManager aliasManager) {
        this.modelRegistry = modelRegistry;
        this.aliasManager = aliasManager;
        
        Config config = modelRegistry.getConfig();
        String logType = config.get("log.type").orElse("none");
        String logPath = config.get("log.path").orElse(System.getProperty("user.home") + "/.llm/logs/conversations");
        
        if (!"none".equalsIgnoreCase(logType)) {
            this.logManager = new LogManager(logType, logPath);
        } else {
            this.logManager = null;
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

    public boolean isLoggingEnabled() {
        return logManager != null;
    }
}
