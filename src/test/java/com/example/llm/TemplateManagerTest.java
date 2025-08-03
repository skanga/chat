package com.example.llm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
public class TemplateManagerTest {
    @TempDir
    File tempDir;

    private TemplateManager templateManager;
    private File templatesDir;

    @BeforeEach
    void setUp() {
        templatesDir = new File(tempDir, ".llm/templates");
        templatesDir.mkdirs();
        templateManager = new TemplateManager(templatesDir);
    }

    @Test
    void shouldLoadTemplate() throws IOException {
        // given
        File templateFile = new File(templatesDir, "test.yaml");
        try (FileWriter writer = new FileWriter(templateFile)) {
            writer.write("prompt: Hello {{name}}!");
        }

        // when
        Template template = templateManager.loadTemplate("test");

        // then
        assertThat(template.getName()).isEqualTo("test");
        assertThat(template.getPrompt()).isEqualTo("Hello {{name}}!");
    }

    @Test
    void shouldThrowForMissingTemplate() {
        // when & then
        assertThatThrownBy(() -> templateManager.loadTemplate("nonexistent"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Template not found");
    }

    @Test
    void shouldRenderTemplate() throws IOException {
        // given
        File templateFile = new File(templatesDir, "test.yaml");
        try (FileWriter writer = new FileWriter(templateFile)) {
            writer.write("prompt: Hello {{name}}!");
        }
        Template template = templateManager.loadTemplate("test");

        // when
        String rendered = template.render(Map.of("name", "World"));

        // then
        assertThat(rendered).isEqualTo("Hello World!");
    }
}
