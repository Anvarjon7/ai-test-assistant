package org.example.aitestassistant.service;

import org.example.aitestassistant.model.FailureContext;

public class AiServiceTest {

    public static void main(String[] args) throws Exception {

        String apikey = System.getenv("GEMINI_API_KEY");
        if (apikey == null || apikey.isBlank()) {
            System.out.println("ERROR: GEMINI_API_KEY not set");
            return;
        }

        AiService service = new AiService(apikey);

        FailureContext context = new FailureContext(
                "calculateTotal_shouldReturnCorrectSum",
                "expected: <15> but was <10>",
                "at.org.example.CalculatorTest.calculateTotal(CalculatorTest.java:24)"
        );

        System.out.println("Calling Gemini API...");
        String result = service.analyze(context);
        System.out.println("Response:\n" + result);
    }
}
