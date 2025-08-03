package com.example.llm;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FragmentManager {

    private final File fragmentsDir;

    public FragmentManager() {
        String userHome = System.getProperty("user.home");
        this.fragmentsDir = new File(userHome, ".llm/fragments");
        if (!fragmentsDir.exists()) {
            fragmentsDir.mkdirs();
        }
    }

    public FragmentManager(File fragmentsDir) {
        this.fragmentsDir = fragmentsDir;
        if (!fragmentsDir.exists()) {
            fragmentsDir.mkdirs();
        }
    }

    public String loadFragment(String name) throws IOException {
        File fragmentFile = new File(fragmentsDir, name);
        if (!fragmentFile.exists()) {
            throw new IOException("Fragment not found: " + name);
        }
        return Files.readString(fragmentFile.toPath());
    }
}
