package com.example.llm;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FragmentManager {

    private final File fragmentsDir;

    public FragmentManager() {
        this(getDefaultFragmentsDir());
    }

    public FragmentManager(File fragmentsDir) {
        this.fragmentsDir = ensureDirectoryExists(fragmentsDir);
    }

    private static File getDefaultFragmentsDir() {
        String userHome = System.getProperty("user.home");
        return new File(userHome, ".llm/fragments");
    }

    private static File ensureDirectoryExists(File directory) {
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory;
    }

    public String loadFragment(String name) throws IOException {
        File fragmentFile = new File(fragmentsDir, name);
        if (!fragmentFile.exists()) {
            throw new IOException("Fragment not found: " + name);
        }
        return Files.readString(fragmentFile.toPath());
    }
}
