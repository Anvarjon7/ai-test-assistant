package org.example.aitestassistant.model;

public class FailureContext {

    private final String testName;
    private final String errorMessage;
    private final String stackTrace;

    public FailureContext(String testName, String errorMessage, String stackTrace) {
        this.testName = testName;
        this.errorMessage = errorMessage;
        this.stackTrace = stackTrace;
    }

    public String getTestName() {
        return testName;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    @Override
    public String toString() {
        return "Test: " + testName + "\nError: " + errorMessage;
    }
}
