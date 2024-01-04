package org.example;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class OpenAIService {
    private OkHttpClient client;
    private MediaType mediaType;
    private String apiKey;

    @Value("${openai.api.key}")
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @PostConstruct
    public void init() {
        client = new OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build();
        mediaType = MediaType.parse("application/json");
    }

    public String getGeneratedCode(String userPrompt) {
        try {
            String response = makeAPICall(userPrompt);
            return extractAndAdjustJavaCode(response);
        } catch (IOException e) {
            throw new RuntimeException("Error while calling OpenAI API", e);
        }
    }

    private String makeAPICall(String prompt) throws IOException {
        JSONObject json = getJsonObject(prompt);

        RequestBody body = RequestBody.create(mediaType, json.toString());
        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();

        Response response = client.newCall(request).execute();
        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

        String responseBody = response.body().string();
        return responseBody;
    }

    private static JSONObject getJsonObject(String userPrompt) {
        JSONObject json = new JSONObject();
        JSONArray messages = new JSONArray();
        JSONObject messageUser = new JSONObject();
        messageUser.put("role", "user");
        messageUser.put("content", userPrompt);
        messages.put(messageUser);
        JSONObject messageSystem = new JSONObject();
        messageSystem.put("role", "system");
        messageSystem.put("content", "You are a Java programming expert providing high quality code.");
        messages.put(messageSystem);
        json.put("messages", messages);
        json.put("max_tokens", 4000);
        json.put("model", "gpt-4-1106-preview");
        json.put("temperature", 0.05);
        return json;
    }

    public String extractJavaCode(String jsonResponse) {
        try {
            System.out.println(jsonResponse);
            JSONObject responseObj = new JSONObject(jsonResponse);

            String messageContent = responseObj.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            JSONObject innerJson = new JSONObject(messageContent);

            return innerJson.getString("java_code");
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    private String extractAndAdjustJavaCode(String jsonResponse) throws JSONException {
        System.out.println(jsonResponse);
        JSONObject responseObj = new JSONObject(jsonResponse);
        JSONArray choicesArray = responseObj.getJSONArray("choices");
        JSONObject firstChoice = choicesArray.getJSONObject(0);
        String messageContent = firstChoice.getJSONObject("message").getString("content");

        String jsonPart = messageContent.substring(messageContent.indexOf("{"), messageContent.lastIndexOf("}") + 1);
        JSONObject jsonCodeObj = new JSONObject(jsonPart);
        String javaCode = jsonCodeObj.getString("java_code");

        javaCode = javaCode.replace("\\n", "\n");  // Convert escaped newlines to actual newlines
        javaCode = javaCode.replace("\\\"", "\""); // Correct escaped quotes

        javaCode = javaCode.replace("your_database", "adoptpethd");
        javaCode = javaCode.replace("your_username", "postgres");
        javaCode = javaCode.replace("your_password", "postgres");
        javaCode = javaCode.replace("yourUsername", "postgres");
        javaCode = javaCode.replace("yourPassword", "postgres");

        if (!javaCode.trim().endsWith(";")) {
            javaCode += ";";
        }
        if (javaCode.contains("IOException") && !javaCode.contains("import java.io.IOException;")) {
            javaCode = "import java.io.IOException;\n" + javaCode;
        }
        System.out.println(javaCode);
        return javaCode;
    }
}
