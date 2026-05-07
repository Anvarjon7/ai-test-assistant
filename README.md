# AI Test Assistant — IntelliJ IDEA Plugin

A lightweight IntelliJ plugin that detects failing tests and shows AI-powered
root cause analysis automatically — no manual prompting needed.

---

## Demo

![demo](docs/demo.gif)

---

## How It Works

When a test fails, the plugin automatically:
1. Captures the test name, error message, and stack trace
2. Filters out framework noise from the stack trace
3. Sends a structured prompt to the Gemini AI API
4. Displays the analysis in a dedicated tool window

---

## Project Structure
src/main/java/org/example/aitestassistant/
├── model/      FailureContext.java          # test failure data
├── service/    AiService.java               # Gemini API call
└── ui/         FailureToolWindowFactory     # listener + UI panel
FailureToolWindowPanel

---

## Key Decisions

- **Message bus subscription** — listener registered per-project via
  `SMTRunnerEventsListener`, fires automatically on test failure
- **Background thread** — AI call runs on `executeOnPooledThread()`,
  never blocks the UI
- **Stack trace filtering** — strips JUnit/IntelliJ frames before
  sending to AI, focusing the model on user code
- **No external dependencies** — uses Java's built-in `HttpClient`
  and manual JSON parsing

---

## Setup

**Requires:** IntelliJ IDEA 2023.3+, JDK 17+, Gemini API key
(free at [aistudio.google.com](https://aistudio.google.com))

```bash
# Set API key (then restart IntelliJ)
setx GEMINI_API_KEY "your-key-here"

# Run plugin
./gradlew runIde
```

---

## Future Improvements

- Include source code context in the prompt
- Settings UI for API key configuration
- Git diff awareness for regression detection
- Local model support for offline usage