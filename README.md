# claude-sonnet-demo

Simple Spring AI (Spring Boot) app with a Thymeleaf chat UI, calling Claude through a LiteLLM/Bedrock gateway.

## Setup

Both secrets must be provided as environment variables (see `AGENTS.md`):

```bash
export ANTHROPIC_BASE_URL=https://llmgw-wp.tekstac.com
export ANTHROPIC_API_KEY=sk-<your-litellm-key>
```

## Run

```bash
mvn spring-boot:run
```

Then open http://localhost:8081 — the Thymeleaf chat page posts to `/` and shows the reply.

## API

```bash
# Plain chat
curl "http://localhost:8081/chat?message=Hello"

# Prompt-template demos (placeholders filled at runtime)
curl "http://localhost:8081/joke?adjective=sarcastic"
curl "http://localhost:8081/travel?city=Tokyo&days=2"

# Tool calling — the model invokes WeatherTools.getCurrentWeather (stubbed)
curl "http://localhost:8081/weather?city=London"
```

## Configuration

`src/main/resources/application.properties` holds the non-secret config; credentials
come from env vars:

| Property | Source |
|---|---|
| `spring.ai.anthropic.base-url` | `${ANTHROPIC_BASE_URL}` |
| `spring.ai.anthropic.api-key` | `${ANTHROPIC_API_KEY}` |
| `spring.ai.anthropic.chat.options.model` | `global.anthropic.claude-haiku-4-5-20251001-v1:0` |
| `server.port` | `8081` |

## Details

- Spring Boot 4.1.2, Spring AI 2.0.1, Java 21, Thymeleaf
- Code: `src/main/java/com/example/springai/` — `ChatController` (REST),
  `ChatViewController` (Thymeleaf view), `PromptDemoController` (joke/travel/weather),
  `PromptTemplateConfig` (reusable `PromptTemplate` beans), `WeatherTools` (`@Tool`)
- Template: `src/main/resources/templates/chat.html`
- Model IDs use the `global.anthropic.*` Bedrock format expected by the gateway
