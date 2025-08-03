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
        String userHome = System.getProperty("user.home");
        this.templatesDir = new File(userHome, ".llm/templates");
        if (!templatesDir.exists()) {
            templatesDir.mkdirs();
        }
    }
    public TemplateManager(File templatesDir) {
        this.templatesDir = templatesDir;
        if (!templatesDir.exists()) {
            templatesDir.mkdirs();
        }
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
