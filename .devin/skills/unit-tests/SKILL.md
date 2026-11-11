---
name: unit-tests
description: Generate JUnit 5 + Mockito unit tests for Service and Controller classes
argument-hint: "<ClassName>"
allowed-tools:
  - read
  - edit
  - grep
  - glob
  - exec
permissions:
  allow:
    - Exec(mvn test)
    - Exec(mvn compile)
---

Generate unit test cases for the class the user specifies (or the currently
open/focused class) using **JUnit 5** and **Mockito**.

## Setup checks

1. Confirm `spring-boot-starter-test` is in `pom.xml` (it bundles JUnit 5,
   Mockito, AssertJ, and Spring Test). If Mockito is missing, add
   `spring-boot-starter-test` — do not add standalone JUnit/Mockito dependencies.
2. Place tests in `src/test/java/com/example/springai/`, mirroring the package
   of the class under test. Name the file `<ClassName>Test.java`.

## Controller classes (`@RestController` / `@Controller`)

- Use `@WebMvcTest(<Controller>.class)` — do NOT spin up the full context.
- Inject `MockMvc` via `@Autowired`.
- Replace collaborators with `@MockitoBean` (Spring Boot 4 / Spring AI 2 — use
  `@MockitoBean`, not the deprecated `@MockBean`). For this project, mock
  `ChatClient.Builder` and stub `builder.build()` to return a mocked
  `ChatClient`.
- Stub the fluent chain: `chatClient.prompt().user(anyString()).call().content()`.
  Use `mock(ChatClient.class, RETURNS_DEEP_STUBS)` if the fluent API makes
  per-call stubbing awkward.
- For `@RestController`: `mockMvc.perform(get("/chat").param("message", "hi"))`
  then `andExpect(status().isOk()).andExpect(content().string(...))`.
- For `@Controller` + Thymeleaf: assert `view().name(...)` and
  `model().attribute(...)`.
- Cover: happy path, default parameter values, and missing/blank params where
  the endpoint makes them meaningful.

## Service classes (`@Service` / plain beans)

- Use `@ExtendWith(MockitoExtension.class)` — no Spring context at all.
- `@Mock` for dependencies, `@InjectMocks` for the class under test, or
  construct it directly in `@BeforeEach` when constructor logic matters.
- Cover: happy path, edge cases (null/empty input), exception propagation, and
  branch coverage. Verify interactions with `verify(mock)` where the call
  matters, and never mock the class under test.

## Style

- JUnit 5 annotations only (`@Test`, `@BeforeEach`, `@ParameterizedTest` for
  input tables) — never JUnit 4 (`@RunWith`, `org.junit.Assert`).
- Naming: `<methodUnderTest>_<scenario>_<expectedResult>`
  e.g. `chat_defaultMessage_returnsContent`.
- Use AssertJ assertions (`assertThat(...)`), not JUnit asserts, when comparing
  objects/strings.
- Use `any()`, `eq()`, `argThat()` correctly — all matchers or none in a call.
- Do not add comments explaining the test; the name should carry intent.
- One logical assertion group per test; use `@ParameterizedTest` instead of
  loops over inputs.

## Verify

After generating, run `mvn test` and iterate until the new tests pass. Report
which test cases were added and their pass/fail status.
