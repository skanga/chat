package com.example.llm;

import java.io.IOException;

public class Llm {

    private final ModelRegistry modelRegistry;
    private final AliasManager aliasManager;

    public Llm() {
        this(new ModelRegistry(new Config()), new AliasManager());
    }

    public Llm(ModelRegistry modelRegistry, AliasManager aliasManager) {
        this.modelRegistry = modelRegistry;
        this.aliasManager = aliasManager;
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
}
