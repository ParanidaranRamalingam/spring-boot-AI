# Project Rules

## Stack
- Java 21, Spring Boot 4.x, Spring AI 2.x (Anthropic starter), Maven, Thymeleaf
- All Java code lives under `src/main/java/com/example/springai/`

## Build & Run
- Build: `mvn clean compile`
- Test: `mvn test`
- Run: `mvn spring-boot:run`
- Always run `mvn compile` after changing Java files to verify the build

## Conventions
- Use `ChatClient.Builder` injected via constructor to build `ChatClient` instances
  (see `ChatController` / `ChatViewController` for the pattern)
- `@RestController` for JSON/text APIs; `@Controller` + Thymeleaf templates for views
- Thymeleaf templates go in `src/main/resources/templates/`
- Configuration goes in `application.properties`; never hardcode secrets — use
  `${ENV_VAR}` placeholders with no inline default for real credentials
- Local secrets live in `./local.properties` (gitignored, imported via
  `spring.config.import`); it overrides `application.properties` values

## Spring AI
- The Anthropic base URL points at a Bedrock gateway (`ANTHROPIC_BASE_URL`), so
  model IDs use the `global.anthropic.*` Bedrock format, not Anthropic-native names
- Reusable prompt templates are `PromptTemplate` beans in `PromptTemplateConfig`,
  rendered with `.create(Map.of(...))` at call time (see `PromptDemoController`)
- Tool functions are `@Tool`-annotated methods on `@Component` classes (see
  `WeatherTools`), registered per call via `.toolCallbacks(ToolCallbacks.from(...))`;
  `ToolCallbacks` lives in `org.springframework.ai.support` in Spring AI 2.x
