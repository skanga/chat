package com.example.llm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AliasManager {
    private final File aliasesFile;
    private final ObjectMapper jsonMapper = new ObjectMapper();

    public AliasManager() {
        this(new File(System.getProperty("user.home"), ".llm"));
    }

    public AliasManager(File llmDir) {
        if (!llmDir.exists()) {
            llmDir.mkdirs();
        }
        this.aliasesFile = new File(llmDir, "aliases.json");
    }

    public Map<String, String> loadAliases() throws IOException {
        if (!aliasesFile.exists()) {
            return new HashMap<>();
        }
        return jsonMapper.readValue(new FileReader(aliasesFile), new TypeReference<Map<String, String>>() {});
    }

    public void saveAliases(Map<String, String> modelAliases) throws IOException {
        try (FileWriter writer = new FileWriter(aliasesFile)) {
            jsonMapper.writerWithDefaultPrettyPrinter().writeValue(writer, modelAliases);
        }
    }

    public void setAlias(String alias, String modelName) throws IOException {
        Map<String, String> aliases = loadAliases();
        aliases.put(alias, modelName);
        saveAliases(aliases);
    }

    public void removeAlias(String alias) throws IOException {
        Map<String, String> aliases = loadAliases();
        aliases.remove(alias);
        saveAliases(aliases);
    }

    public String resolveAlias(String modelName) throws IOException {
        Map<String, String> aliases = loadAliases();
        return aliases.getOrDefault(modelName, modelName);
    }
}
