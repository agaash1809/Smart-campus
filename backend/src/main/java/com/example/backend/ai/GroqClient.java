package com.example.backend.ai;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GroqClient {

     @Value("${groq.api.key}")
     private String API_KEY;
    private static final String ENDPOINT = "https://api.groq.com/openai/v1/chat/completions";

  
    private static final String MODEL = "llama-3.3-70b-versatile";


    private final OkHttpClient client = new OkHttpClient();

    public String ask(String prompt) throws Exception {

       

        JSONObject json = new JSONObject();
        json.put("model", MODEL);
        json.put("temperature", 0.2);
        json.put("max_tokens", 800);

        JSONArray messages = new JSONArray();
        messages.put(new JSONObject()
                .put("role", "system")
                .put("content",
                        "You are Jarvis. Answer ONLY using the provided context. If unknown, say exactly 'I don't know.'")
        );

        messages.put(new JSONObject()
                .put("role", "user")
                .put("content", prompt)
        );

        json.put("messages", messages);

        RequestBody body = RequestBody.create(json.toString(), MediaType.get("application/json"));

        Request request = new Request.Builder()
                .url(ENDPOINT)
                .post(body)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {

            String resp = response.body().string();
            System.out.println("GROQ RAW RESPONSE: " + resp);

            if (!response.isSuccessful()) {
                return "Groq API Error: " + resp;
            }

            JSONObject parsed = new JSONObject(resp);

            if (!parsed.has("choices")) {
                return "Groq Error: no 'choices' field. Full response: " + resp;
            }

            JSONObject choice = parsed.getJSONArray("choices").getJSONObject(0);
            JSONObject msg = choice.getJSONObject("message");

            return msg.getString("content");
        }
    }
}
