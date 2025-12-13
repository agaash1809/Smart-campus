package com.example.backend.controller;

import com.example.backend.ai.GroqClient;
import com.example.backend.index.IndexService;
import com.example.backend.store.DocStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AskController {

    @Autowired
    private IndexService indexService;

    @Autowired
    private GroqClient groq;

    // ---------------------- DTO CLASSES ----------------------
    public static class AskRequest {
        public String docId;
        public String question;
    }

    public static class ChunkInfo {
        public String docId;
        public String chunkId;
        public String text;
        public float score;

        public ChunkInfo(String docId, String chunkId, String text, float score) {
            this.docId = docId;
            this.chunkId = chunkId;
            this.text = text;
            this.score = score;
        }
    }

    public static class AskResponse {
        public String answer;
        public List<ChunkInfo> matches;

        public AskResponse(String answer, List<ChunkInfo> matches) {
            this.answer = answer;
            this.matches = matches;
        }
    }

    // ---------------------- ASK ENDPOINT ----------------------
    @PostMapping("/ask")
public ResponseEntity<?> ask(@RequestBody AskRequest req) {
    try {
        if (req == null || req.question == null || req.question.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Question is required"));
        }

        List<IndexService.SearchResult> results =
                indexService.search(req.question, req.docId, 5);

        if (results.isEmpty()) {
            return ResponseEntity.ok(new AskResponse("I don't know.", List.of()));
        }

        List<ChunkInfo> matches = results.stream()
                .map(r -> new ChunkInfo(r.docId, r.chunkId, r.content, r.score))
                .collect(Collectors.toList());

        StringBuilder context = new StringBuilder();
        for (IndexService.SearchResult r : results) {
            context.append(r.content.trim()).append("\n\n");
        }

        String prompt =
                "Answer ONLY using this context. If answer is missing, reply exactly 'I don't know.'\n"
              + "Do NOT mention chunks, do NOT print sources, do NOT reference metadata.\n"
              + "Give a clean, direct answer ONLY.\n\n"
              + "CONTEXT:\n" + context.toString()
              + "\nQUESTION:\n" + req.question;

        String rawAnswer = groq.ask(prompt);

        // ------------- FINAL CLEANING FIX -----------------
        String clean = Arrays.stream(rawAnswer.split("\\n"))
                .filter(line -> !line.toLowerCase().contains("chunk"))
                .filter(line -> !line.toLowerCase().contains("source"))
                .filter(line -> !line.trim().matches("(?i).*chunk.*"))
                .filter(line -> !line.trim().matches("(?i).*source.*"))
                .collect(Collectors.joining("\n"))
                .trim();

        clean = clean.replaceAll("\\n{2,}", "\n").trim();
        // ---------------------------------------------------

        return ResponseEntity.ok(new AskResponse(clean, matches));

    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
    }
}


    // ---------------------- SUMMARY ENDPOINT ----------------------
   @GetMapping("/summarize/{docId}")
public ResponseEntity<?> summarize(@PathVariable String docId) {
    try {
        String text = DocStore.DOC_TEXTS.get(docId);
        if (text == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Document not found"));
        }

        String prompt =
                "Provide a single 20-25 line, well-structured summary of the following document. " +
                "The summary must be clear, concise, and cover all important points. " +
                "Do NOT create multiple sections like Level-1 or Level-2. " +
                "Give ONLY ONE combined summary.\n\n" +
                "Document:\n" + text;

        String result = groq.ask(prompt);

        return ResponseEntity.ok(Map.of(
                "docId", docId,
                "summary", result
        ));

    } catch (Exception e) {
        return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
    }
}

    // ---------------------- QUIZ ENDPOINT ----------------------
    @GetMapping("/quiz/{docId}")
    public ResponseEntity<?> quiz(@PathVariable String docId) {
        try {
            String text = DocStore.DOC_TEXTS.get(docId);
            if (text == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Document not found"));
            }

            // String prompt =
            //         "Generate a quiz strictly from the document below.\n"
            //         + "Create 10 MCQs.\n"
            //         + "Each MCQ must have 4 options: A, B, C, D.\n"
            //         + "Clearly mark the correct answer.\n\n"
            //         + "Document:\n" + text;

            String prompt =
        "Generate 10 MCQ questions strictly from the given document.\n" +
        "Return the output ONLY in JSON format, exactly like this:\n\n" +
        "{ \"quiz\": [\n" +
        "  {\"q\": \"question text\", \"options\": [\"Option A\",\"Option B\",\"Option C\",\"Option D\"], \"answer\": 1},\n" +
        "  ... (10 questions)\n" +
        "]}\n\n" +
        "Where 'answer' is the index of the correct option (0=A,1=B,2=C,3=D).\n" +
        "Do NOT include explanations, markdown, or extra text.\n\n" +
        "Document:\n" + text;


            String quiz = groq.ask(prompt);

            return ResponseEntity.ok(Map.of(
                    "docId", docId,
                    "quiz", quiz
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ---------------------- DOC LIST ENDPOINT ----------------------
    @GetMapping("/docs")
    public ResponseEntity<?> listDocs() {
        return ResponseEntity.ok(DocStore.DOC_TEXTS.keySet());
    }
}
