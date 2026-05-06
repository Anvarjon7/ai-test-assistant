package org.example.aitestassistant.listener;

import com.intellij.execution.testframework.sm.runner.SMTRunnerEventsAdapter;
import com.intellij.execution.testframework.sm.runner.SMTestProxy;
import com.intellij.openapi.application.ApplicationManager;
import org.example.aitestassistant.model.FailureContext;
import org.example.aitestassistant.service.AiService;
import org.example.aitestassistant.ui.FailureToolWindowFactory;
import org.jetbrains.annotations.NotNull;

public class MySMTestListener extends SMTRunnerEventsAdapter {

    private final AiService aiService;

    public MySMTestListener() {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isBlank()){
            throw new IllegalStateException("GEMINI_API_KEY environment variable is not set");
        }

        this.aiService = new AiService(apiKey);
    }

    @Override
    public void onTestFailed(@NotNull SMTestProxy test) {

        FailureContext context = new FailureContext(
                test.getName(),
                test.getErrorMessage(),
                test.getStacktrace()
        );

        if (FailureToolWindowFactory.panel != null) {
            FailureToolWindowFactory.panel.showLoading();
        }

        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                String result = aiService.analyze(context);

                if (FailureToolWindowFactory.panel != null) {
                    FailureToolWindowFactory.panel.showResult(formatResult(result));
                }
            }catch (Exception e) {
                if (FailureToolWindowFactory.panel != null) {
                    FailureToolWindowFactory.panel.showError(e.getMessage());
                }
            }
        });
    }


    private String formatResult(String jsonResponse) {

        String rootCause = extractField(jsonResponse, "rootCause");
        String explanation = extractField(jsonResponse, "explanation");
        String suggestedFix = extractField(jsonResponse, "suggestedFix");

        return "ROOT CAUSE\n" + rootCause +
                "\n\nEXPLANATION\n" + explanation +
                "\n\nSUGGESTED FIX\n" + suggestedFix;
    }

    private String extractField(String json, String fieldName) {
        String marker = "\"" + fieldName + "\": \"";
        int start = json.indexOf(marker);
        if (start == -1) {
            marker = "\"" + fieldName + "\":\"";
            start = json.indexOf(marker);
        }
        if (start == -1) return "N/A";

        start += marker.length();
        int end = start;
        while (end < json.length()) {
            if (json.charAt(end) == '"' && json.charAt(end - 1) != '\\') break;
            end++;
        }
        return json.substring(start, end)
                .replace("\\n", "\n")
                .replace("\\\"", "\"");
    }
}

