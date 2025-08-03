package com.example.llm;

import dev.langchain4j.agent.tool.Tool;

public class Tools {

    @Tool("Calculates the length of a string")
    public int stringLength(String s) {
        return s.length();
    }
}
