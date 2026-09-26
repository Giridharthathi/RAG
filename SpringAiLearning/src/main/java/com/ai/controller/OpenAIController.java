package com.ai.controller;

import com.ai.service.PromptService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
public class OpenAIController {

    private final ChatClient chatClient;
    private final PgVectorStore vectorStore;
    private final PromptService promptTemplate;

    public OpenAIController(ChatClient.Builder chatClient, PgVectorStore vectorStore, PromptService promptTemplate) {
        this.chatClient = chatClient.build();
        this.vectorStore = vectorStore;
        this.promptTemplate = promptTemplate;
    }


    @GetMapping("/ask")
    public ResponseEntity<String> openMethod(@RequestParam String query, @RequestParam String userId) throws IOException {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .filterExpression("userId == '"+userId+"'" )
                .topK(5).build();

        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        if(documents.isEmpty()){
            return new ResponseEntity<>("No relevant doc found for this query", HttpStatus.NOT_FOUND);
        }
        String context = documents.stream().map(Document::getText).collect(Collectors.joining("\n"));
        Prompt prompt = promptTemplate.createPrompt(context, query);
        String content = chatClient.prompt(prompt)
                .call().content();
        return new ResponseEntity<>(content, HttpStatus.OK);
    }

    @PostMapping("/upload/doc")
    public ResponseEntity<String> saveDoc(@RequestParam MultipartFile file,
                                          @RequestParam String userId) {
        DocumentReader documentReader = new TikaDocumentReader(file.getResource());
        TextSplitter textSplitter = new TokenTextSplitter();
        List<Document> documents = textSplitter.split(documentReader.read());

        documents.forEach(
                document -> document.getMetadata().put("userId", userId)
        );

        vectorStore.add(documents);
        return new ResponseEntity<>("Uploaded successfully, Please enter your queries",
                HttpStatus.OK);
    }
}