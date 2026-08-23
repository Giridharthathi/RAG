package com.ai.controller;

import org.springframework.ai.chat.client.ChatClient;
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

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
public class OpenAIController {

    private final ChatClient chatClient;
    private final PgVectorStore vectorStore;

    public OpenAIController(ChatClient.Builder chatClient, PgVectorStore vectorStore) {
        this.chatClient = chatClient.build();
        this.vectorStore = vectorStore;
    }

    @GetMapping("/ask")
    public ResponseEntity<String> openMethod(@RequestParam String query) {
        SearchRequest searchRequest = SearchRequest.builder()
                        .query(query).topK(5).build();

        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        String context = documents.stream().map(Document::getText).collect(Collectors.joining("\n"));
        String content = chatClient.prompt()
                .user("""
                        Answer the question using only the provided context.
                        Context:
                        %s
   
                        Question:
                        %s
                        """.formatted(context, query))
                .call().content();
        return new ResponseEntity<>(content, HttpStatus.OK);
    }

    @PostMapping("/upload/doc")
    public ResponseEntity<String> saveDoc(@RequestParam MultipartFile file) {
        DocumentReader documentReader = new TikaDocumentReader(file.getResource());
        TextSplitter textSplitter = new TokenTextSplitter();
        List<Document> documents = textSplitter.split(documentReader.read());

        vectorStore.add(documents);
        return new ResponseEntity<>("Uploaded successfully, Please enter your queries", HttpStatus.OK);
    }
}