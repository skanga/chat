package com.example.llm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class FragmentManagerTest {
    @TempDir
    File tempDir;

    private FragmentManager fragmentManager;
    private File fragmentsDir;

    @BeforeEach
    void setUp() {
        fragmentsDir = new File(tempDir, ".llm/fragments");
        fragmentsDir.mkdirs();
        fragmentManager = new FragmentManager(fragmentsDir);
    }

    @Test
    void shouldLoadFragment() throws IOException {
        // given
        File fragmentFile = new File(fragmentsDir, "test-fragment");
        try (FileWriter writer = new FileWriter(fragmentFile)) {
            writer.write("This is a fragment.");
        }

        // when
        String fragment = fragmentManager.loadFragment("test-fragment");

        // then
        assertThat(fragment).isEqualTo("This is a fragment.");
    }

    @Test
    void shouldThrowForMissingFragment() {
        // when & then
        assertThatThrownBy(() -> fragmentManager.loadFragment("nonexistent"))
                .isInstanceOf(IOException.class)
                .hasMessage("Fragment not found: nonexistent");
    }
}
