package com.example.llm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AliasManagerTest {
    @TempDir
    File tempDir;

    private AliasManager aliasManager;

    @BeforeEach
    void setUp() {
        File llmDir = new File(tempDir, ".llm");
        llmDir.mkdirs();
        aliasManager = new AliasManager(llmDir);
    }

    @Test
    void shouldSetAndGetAlias() throws IOException {
        // when
        aliasManager.setAlias("my-model", "openai/gpt-4o");

        // then
        Map<String, String> aliases = aliasManager.loadAliases();
        assertThat(aliases).containsEntry("my-model", "openai/gpt-4o");
    }

    @Test
    void shouldRemoveAlias() throws IOException {
        // given
        aliasManager.setAlias("my-model", "openai/gpt-4o");

        // when
        aliasManager.removeAlias("my-model");

        // then
        Map<String, String> aliases = aliasManager.loadAliases();
        assertThat(aliases).doesNotContainKey("my-model");
    }

    @Test
    void shouldResolveAlias() throws IOException {
        // given
        aliasManager.setAlias("my-model", "openai/gpt-4o");

        // when
        String resolved = aliasManager.resolveAlias("my-model");

        // then
        assertThat(resolved).isEqualTo("openai/gpt-4o");
    }

    @Test
    void shouldReturnRriginalNameIfNoAlias() throws IOException {
        // when
        String resolved = aliasManager.resolveAlias("unaliased-model");

        // then
        assertThat(resolved).isEqualTo("unaliased-model");
    }
}
