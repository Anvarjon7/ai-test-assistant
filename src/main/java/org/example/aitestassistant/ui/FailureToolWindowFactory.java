package org.example.aitestassistant.ui;

import com.intellij.execution.testframework.sm.runner.SMTRunnerEventsAdapter;
import com.intellij.execution.testframework.sm.runner.SMTRunnerEventsListener;
import com.intellij.execution.testframework.sm.runner.SMTestProxy;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.util.messages.MessageBusConnection;
import org.example.aitestassistant.model.FailureContext;
import org.example.aitestassistant.service.AiService;
import org.jetbrains.annotations.NotNull;

public class FailureToolWindowFactory implements ToolWindowFactory {

    public static FailureToolWindowPanel panel;

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        panel = new FailureToolWindowPanel();

        ContentFactory contentFactory = ContentFactory.getInstance();
        Content content = contentFactory.createContent(panel.getContent(), "", false);
        toolWindow.getContentManager().addContent(content);

        // AiService created once per project, not per failure
        String apiKey = System.getenv("GEMINI_API_KEY");
        AiService aiService = new AiService(apiKey);

        MessageBusConnection connection = project.getMessageBus().connect();
        connection.subscribe(SMTRunnerEventsListener.TEST_STATUS, new SMTRunnerEventsAdapter() {
            @Override
            public void onTestFailed(@NotNull SMTestProxy test) {
                FailureContext context = new FailureContext(
                        test.getName(),
                        test.getErrorMessage(),
                        test.getStacktrace()
                );

                panel.showLoading();

                ApplicationManager.getApplication().executeOnPooledThread(() -> {
                    try {
                        String result = aiService.analyze(context);
                        panel.showResult(formatResult(result));
                    } catch (Exception e) {
                        panel.showError(e.getMessage());
                    }
                });
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