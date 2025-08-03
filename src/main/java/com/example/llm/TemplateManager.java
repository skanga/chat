package com.example.llm;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class TemplateManager {
    private final File templatesDir;

    public TemplateManager() {
        this(getDefaultTemplatesDir());
    }

    public TemplateManager(File templatesDir) {
        this.templatesDir = ensureDirectoryExists(templatesDir);
    }

    private static File getDefaultTemplatesDir() {
        String userHome = System.getProperty("user.home");
        return new File(userHome, ".llm/templates");
    }

    private static File ensureDirectoryExists(File directory) {
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory;
    }

    public Template loadTemplate(String name) throws IOException {
        File templateFile = new File(templatesDir, name + ".yaml");
        if (!templateFile.exists()) {
            throw new FileNotFoundException("Template not found: " + name);
        }

        Yaml yaml = new Yaml();
        try (InputStream inputStream = new FileInputStream(templateFile)) {
            Map<String, Object> data = yaml.load(inputStream);
            String prompt = (String) data.get("prompt");
            return new Template(name, prompt);
        }
    }
}
