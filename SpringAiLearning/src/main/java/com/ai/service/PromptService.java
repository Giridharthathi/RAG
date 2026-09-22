package com.ai.service;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class PromptService {
    private final Resource promptResource;

    public PromptService(@Value("classpath:prompts/rag-prompt.txt")Resource promptResource) {
        this.promptResource = promptResource;
    }

    public Prompt createPrompt(String context, String question) throws IOException {
        String template = new String(promptResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        PromptTemplate promptTemplate = new PromptTemplate(template);

        Map<String, Object> val = Map.of(
                "context", context,
                "question", question
        );

        return promptTemplate.create(val);
    }
}
