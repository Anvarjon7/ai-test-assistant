package org.example.aitestassistant.service;

import org.example.aitestassistant.model.FailureContext;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AiService {

    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=";

    private final String apiKey;
    private final HttpClient httpClient;

    public AiService(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newHttpClient();
    }

    public String analyze(FailureContext context) throws Exception {
        String prompt = buildPrompt(context);
        String requestBody = buildRequestBody(prompt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + apiKey))
                .header("Content-Type", "application.json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            throw new RuntimeException("API error: " + response.statusCode()
                    + "\n" + response.body());
        }

        return parseResponse(response.body());
    }

    private String buildPrompt(FailureContext context) {
        return "You are a senior software engineer analyzing a failing unit test. \n\n" +
                "Test name: " + context.getTestName() + "\n" +
                "Error message: " + context.getErrorMessage() + "\n" +
                "Stack trace:\n" + filterStacktrace(context.getStackTrace()) + "\n\n" +
                "Respond ONLY as valid JSON with exactly these three fields:\n" +
                "{\n" +
                " \"rootCause\": \"one sentence explanation\",\n" +
                " \"explanation\": \"2-3 sentences in plain language\",\n" +
                " \"suggestedFix\": \"concrete fix or code change\"n" +
                "}";
    }

    private String filterStacktrace(String stackTrace){
        if (stackTrace == null || stackTrace.isBlank()) {
            return "No stack trace available";
        }

        return stackTrace.lines()
                .filter(line -> !line.contains("java.base/")
                        && !line.contains("org.junit")
                        && !line.contains("com.intellij")
                        && !line.contains("sun.reflect")
                        && !line.contains("java.lang.reflect"))
                .reduce("", (a,b) -> a + "\n" + b)
                .trim();
    }

    private String buildRequestBody(String prompt) {
        String escaped = prompt
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

        return "{\n" +
                "  \"contents\": [\n" +
                "    {\n" +
                "      \"parts\": [\n" +
                "        {\"text\": \"" + escaped + "\"}\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    private String parseResponse(String responseBody) {
        String marker = "\"text\": \"";
        int start = responseBody.indexOf(marker);
        if (start == -1) {
            marker = "\"text\":\"";
            start = responseBody.indexOf(marker);
        }

        if (start == -1) {
            throw new RuntimeException("Unexpected response format:\n" + responseBody);
        }

        start += marker.length();

        int end = start;
        while (end < responseBody.length()) {
            if (responseBody.charAt(end) == '"' && responseBody.charAt(end - 1) != '\\') {
                break;
            }
            end++;
        }

        return responseBody.substring(start, end)
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }
}


