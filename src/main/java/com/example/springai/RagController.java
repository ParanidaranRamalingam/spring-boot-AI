package com.example.springai;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RagController {

    private static final double SIMILARITY_THRESHOLD = 0.3;

    private final EmbeddingModel embeddingModel;
    private final ChatClient chatClient;
    private final List<EmbeddedChunk> chunks;

    public RagController(ChatClient.Builder chatClientBuilder,
                         EmbeddingModel embeddingModel) throws IOException {
        this.embeddingModel = embeddingModel;

        // Split policy.md into one Document per "## Section" heading, then
        // embed all chunks in one batch at startup.
        String policy = new ClassPathResource("policy.md")
                .getContentAsString(StandardCharsets.UTF_8);
        List<Document> docs = Arrays.stream(policy.split("(?m)(?=^## )"))
                .map(String::trim)
                .filter(section -> !section.isBlank())
                .map(section -> Document.builder().text(section).build())
                .toList();
        List<float[]> embeddings = embeddingModel.embed(
                docs.stream().map(Document::getText).toList());

        this.chunks = IntStream.range(0, docs.size())
                .mapToObj(i -> new EmbeddedChunk(docs.get(i), embeddings.get(i)))
                .toList();

        this.chatClient = chatClientBuilder
                .defaultAdvisors(RetrievalAugmentationAdvisor.builder()
                        .documentRetriever(this::retrieve)
                        // If nothing is similar enough, say so instead of guessing.
                        .queryAugmenter(ContextualQueryAugmenter.builder()
                                .allowEmptyContext(false)
                                .build())
                        .build())
                .build();
    }

    /**
     * GET /rag/ask?question=...
     * Matches on meaning, not keywords: "how much time off do I get?"
     * retrieves the vacation section even though "time off" never appears in it.
     */
    @GetMapping("/rag/ask")
    public String ask(@RequestParam(defaultValue = "How many vacation days do I get?") String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }

    /**
     * GET /rag/sources?question=...
     * The retrieved chunks with their cosine similarity scores — shows how
     * close the question is to each policy section.
     */
    @GetMapping("/rag/sources")
    public List<SourceHit> sources(
            @RequestParam(defaultValue = "How many vacation days do I get?") String question) {
        return scoredChunks(question).stream()
                .map(scored -> new SourceHit(scored.score(), scored.doc().getText()))
                .toList();
    }

    /**
     * GET /rag/similarity?text1=...&text2=...
     * Raw demo of embeddings + cosine similarity: embeds two arbitrary texts and
     * returns their score in [-1, 1]. Try text1="cat"&text2="kitten" vs text2="car".
     */
    @GetMapping("/rag/similarity")
    public String similarity(@RequestParam String text1, @RequestParam String text2) {
        List<float[]> vectors = embeddingModel.embed(List.of(text1, text2));
        return "cosine similarity = %.4f".formatted(
                cosineSimilarity(vectors.get(0), vectors.get(1)));
    }

    /** Cosine similarity between two vectors of equal length, in [-1, 1]. */
    static double cosineSimilarity(float[] a, float[] b) {
        double dot = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return (normA == 0.0 || normB == 0.0) ? 0.0
                : dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /** Embed the question and keep the 4 most similar chunks above the threshold. */
    private List<Document> retrieve(Query query) {
        return scoredChunks(query.text()).stream()
                .map(ScoredChunk::doc)
                .toList();
    }

    private List<ScoredChunk> scoredChunks(String text) {
        float[] questionVector = embeddingModel.embed(text);
        return chunks.stream()
                .map(chunk -> new ScoredChunk(chunk.doc(),
                        cosineSimilarity(questionVector, chunk.embedding())))
                .filter(scored -> scored.score() >= SIMILARITY_THRESHOLD)
                .sorted(Comparator.comparingDouble(ScoredChunk::score).reversed())
                .limit(4)
                .toList();
    }

    /** JSON shape for /rag/sources: a chunk and its similarity score. */
    public record SourceHit(double score, String text) {
    }

    private record EmbeddedChunk(Document doc, float[] embedding) {
    }

    private record ScoredChunk(Document doc, double score) {
    }
}
