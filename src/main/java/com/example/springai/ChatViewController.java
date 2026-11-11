package com.example.springai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ChatViewController {

    private final ChatClient chatClient;

    public ChatViewController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @GetMapping("/")
    public String chatPage() {
        return "chat";
    }

    @GetMapping("/demo")
    public String demoPage() {
        return "demo";
    }

    @PostMapping("/")
    public String sendMessage(@RequestParam String message, Model model) {
        model.addAttribute("userMessage", message);
        try {
            model.addAttribute("response", chatClient.prompt().user(message).call().content());
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "chat";
    }
}
