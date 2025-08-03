package com.example.llm;

import java.util.Map;

public class Template {
    private final String name;
    private final String prompt;

    public Template(String name, String prompt) {
        this.name = name;
        this.prompt = prompt;
    }

    public String getName() {
        return name;
    }

    public String getPrompt() {
        return prompt;
    }

    public String render(Map<String, String> params) {
        String renderedPrompt = prompt;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            renderedPrompt = renderedPrompt.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return renderedPrompt;
    }
}
