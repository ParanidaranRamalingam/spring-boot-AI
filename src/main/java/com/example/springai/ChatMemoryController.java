package com.example.springai;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatMemoryController {

    private final ChatMemory chatMemory;
    private final ChatClient chatClient;

    public ChatMemoryController(ChatClient.Builder chatClientBuilder) {
        // In-memory store keeping the last 20 messages per conversation.
        // Swap in a repository-backed ChatMemory (JDBC, Cassandra, ...) to persist.
        this.chatMemory = MessageWindowChatMemory.builder().build();
        this.chatClient = chatClientBuilder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    /**
     * GET /memory/ask?message=...&conversationId=...
     * Try "my name is Ada and I like hiking", then "what's my name?" — same
     * conversationId remembers; a different one doesn't.
     */
    @GetMapping("/memory/ask")
    public String ask(
            @RequestParam(defaultValue = "Hi, my name is Ada and I like hiking") String message,
            @RequestParam(defaultValue = "demo") String conversationId) {
        return chatClient.prompt()
                .user(message)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

    /**
     * GET /memory/history?conversationId=...
     * Shows exactly what the advisor will inject on the next call — every
     * stored user/assistant message for that conversation.
     */
    @GetMapping("/memory/history")
    public List<MemoryMessage> history(@RequestParam(defaultValue = "demo") String conversationId) {
        return chatMemory.get(conversationId).stream()
                .map(message -> new MemoryMessage(message.getMessageType().getValue(), message.getText()))
                .toList();
    }

    /** JSON shape for /memory/history: a stored message and its role. */
    public record MemoryMessage(String role, String text) {
    }
}
