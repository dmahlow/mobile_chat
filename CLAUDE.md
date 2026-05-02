# GPT Mobile (fork: dmahlow/mobile_chat)

Fork of [Taewan-P/gpt_mobile](https://github.com/Taewan-P/gpt_mobile) - Android chat app supporting multiple AI providers simultaneously.

## Build

```bash
./gradlew assembleDebug          # debug APK
./gradlew assembleRelease        # release APK (minified)
./gradlew bundleRelease          # AAB for Play Store
./gradlew test                   # unit tests (JVM)
./gradlew connectedAndroidTest   # instrumented tests (device/emulator)
```

Linting: ktlint 1.3.1 via GitHub Actions. No local lint task configured.

## Architecture

Kotlin-only, single-module Android app. Jetpack Compose UI, Hilt DI, Room DB, Ktor networking.

```
app/src/main/kotlin/dev/chungjungsoo/gptmobile/
  data/
    context/       - ContextBuilder, ProviderContextPolicy (turn windowing, attachment stripping)
    database/      - Room entities, DAOs, migrations (V1 legacy + V2 current)
    datastore/     - DataStore preferences for settings
    dto/           - API request/response DTOs per provider (anthropic/, openai/, google/, groq/)
    model/         - Domain enums (ApiType, ClientType, ChatAttachment, ThemeMode)
    network/       - API interfaces + impls (OpenAI, Anthropic, Google, Groq) + NetworkClient (Ktor)
    repository/    - ChatRepository (completions, DB ops), SettingRepository, AttachmentUploadCoordinator
  di/              - Hilt modules
  presentation/
    common/        - Navigation routes, shared composables, ThemeViewModel
    icons/         - Custom icon composables
    theme/         - Material 3 theming
    ui/
      chat/        - ChatScreen, ChatViewModel, markdown rendering, ThinkingBlock
      home/        - Chat list
      main/        - MainActivity, MainViewModel
      migrate/     - V1-to-V2 database migration UI
      setting/     - Platform config, about, licenses
      setup/       - First-run platform wizard
  util/            - Extensions, FileUtils, AttachmentPayloadCache, AssistantMessageUtils
```

## Supported Providers

Defined in `ClientType.kt`: OPENAI, ANTHROPIC, GOOGLE, GROQ, OPENROUTER, OLLAMA, CUSTOM.

Each provider has a dedicated API interface + impl in `data/network/`. All streaming responses emit `Flow<ApiState>`.

Default API URLs and model lists in `ModelConstants.kt`.

## Key Patterns

- **Context windowing**: `ContextBuilder` assembles conversation history per-provider. `ProviderContextPolicy` controls turn window size (6-10 turns) and image attachment retention window. Failed/empty assistant turns are filtered out.
- **Attachment pipeline**: `AttachmentUploadCoordinator` handles per-provider file upload (OpenAI Files API, Anthropic base64, Google upload). `AttachmentPayloadCache` prevents re-encoding large payloads.
- **Assistant revisions**: Messages support multiple revision snapshots (edits by user or retry). `activeRevisionIndex` selects which to display; `effectiveContent()` resolves the displayed text.
- **Groq reasoning**: `GroqReasoningParser` extracts `<think>` blocks from Groq responses into separate thought content.
- **Error handling**: `ApiStateFlowExtensions` + `AssistantMessageUtils` parse error markers like `[Response stopped: ...]` and strip them before context re-submission.

## Database

Two Room databases coexist during migration:
- `ChatDatabase` (V1, legacy) - simple messages table
- `ChatDatabaseV2` (current) - chat rooms, messages with revisions/attachments, platform models

Schema exports in `app/schemas/`. Migration logic in `ChatDatabaseV2Migrations.kt`.

## SDK Targets

- minSdk 31 (Android 12)
- targetSdk/compileSdk 36
- Java 17
- Kotlin 2.3.x
- Gradle with Kotlin DSL, version catalog at `gradle/libs.versions.toml`
