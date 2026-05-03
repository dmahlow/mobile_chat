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

Release APK is unsigned. To install on device, sign with debug key:
```bash
zipalign -f 4 app/build/outputs/apk/release/app-release-unsigned.apk /tmp/signed.apk
apksigner sign --ks ~/.android/debug.keystore --ks-key-alias androiddebugkey --ks-pass pass:android /tmp/signed.apk
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
    dto/           - API request/response DTOs per provider (anthropic/, openai/, google/, groq/, brave/, toolcalling/)
    model/         - Domain enums (ApiType, ClientType, ChatAttachment, ThemeMode, SharedContentHolder)
    network/       - API interfaces + impls (OpenAI, Anthropic, Google, Groq, BraveSearch) + NetworkClient (Ktor)
    repository/    - ChatRepository, SettingRepository, AttachmentUploadCoordinator, ToolCallOrchestrator
  di/              - Hilt modules
  presentation/
    common/        - Navigation routes, shared composables, ThemeViewModel
    icons/         - Custom icon composables
    theme/         - Material 3 theming (Source Serif 4 + DM Sans, warm cream palette)
    ui/
      chat/        - ChatScreen, ChatViewModel, StreamingChatMarkdown, ToolUseIndicator, ThinkingBlock
      home/        - Chat list
      main/        - MainActivity (share intent handling), MainViewModel
      migrate/     - V1-to-V2 database migration UI
      setting/     - Platform config, web search settings, about, licenses
      setup/       - First-run platform wizard
  util/            - Extensions, FileUtils, AttachmentPayloadCache, AssistantMessageUtils, ApiStateFlowExtensions
```

## Supported Providers

Defined in `ClientType.kt`: OPENAI, ANTHROPIC, GOOGLE, GROQ, OPENROUTER, OLLAMA, CUSTOM.

Each provider has a dedicated API interface + impl in `data/network/`. All streaming responses emit `Flow<ApiState>`.

Default API URLs and model lists in `ModelConstants.kt`.

## Tool Calling

The app supports LLM tool/function calling via `ToolCallOrchestrator`:

- **web_search**: Brave Search API (user configures key in Settings). Active on Chat Completions path (OpenRouter, Ollama, Custom providers).
- **get_current_datetime**: Returns device clock (date, time, day, timezone, UTC offset). Zero-arg tool.

Flow: stream response -> detect `ToolCallChunk` states -> orchestrator accumulates tool call -> executes tool -> re-submits conversation with results -> streams final answer.

Tool definitions are sent as OpenAI-format `tools` array. DTOs also prepared for Anthropic/Google formats (not yet wired into those streaming paths).

Tool usage is tracked via `[tools:name1,name2]` prefix in the message thoughts field, parsed by `parseToolMarkers()` in ChatBubble.kt for UI display.

## Share Sheet

The app registers as a share target for text/*, image/*, application/pdf. Shared content is stored in `SharedContentHolder` and consumed by `ChatViewModel` on new chat creation. `HomeScreen` auto-navigates to a new chat when pending shared content exists.

## Key UI Patterns

- **Chat LazyColumn uses `reverseLayout = true`**: newest messages at bottom (index 0). Streaming content grows upward naturally without manual scroll logic.
- **Streaming text fade**: `StreamingChatMarkdown` applies a gradient overlay (background -> transparent) on the bottom 48dp during streaming.
- **Smooth text push**: `animateContentSize` on the streaming markdown (conditionally, only while `isLoading`). Removed on completion to prevent jump.
- **Collapsing header**: Custom `NestedScrollConnection` updates TopAppBar state without consuming scroll delta, so content and bar move simultaneously.
- **Action menu**: Collapsed behind a `...` button, expands inline. Uses `AnimatedVisibility(fadeIn/fadeOut)` to avoid layout jump.
- **Typography**: Source Serif 4 (bundled TTF) for assistant prose, DM Sans for UI chrome. Defined in `Type.kt`.
- **Color palette**: Warm cream background (#F5F0E8 light, #1A1917 dark), amber-orange primary (#CC6A35).

## Key Patterns (Data Layer)

- **Context windowing**: `ContextBuilder` assembles conversation history per-provider. `ProviderContextPolicy` controls turn window size (6-10 turns) and image attachment retention window.
- **Attachment pipeline**: `AttachmentUploadCoordinator` handles per-provider file upload. `AttachmentPayloadCache` prevents re-encoding large payloads.
- **Assistant revisions**: Messages support multiple revision snapshots. `activeRevisionIndex` selects which to display; `effectiveContent()` resolves the displayed text.
- **Streaming buffer**: `StreamingMessageBuffer` in ApiStateFlowExtensions throttles UI updates to every 50ms, tracks tool usage, publishes content + thoughts + tool markers.

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

## Known Issues / Technical Debt

- `orchestrate()` function in ToolCallOrchestrator is dead code (never collects tool calls). Only `orchestrateWithToolDetection()` works.
- Tool calling only active on Chat Completions path. OpenAI Responses API and Anthropic/Google native paths don't have tool support yet.
- Downloadable Google Fonts code still in font_certs.xml / libs.versions.toml (unused, fonts are bundled as TTF now).
- `isImageFile()` defined in both ChatBubble.kt and ChatScreen.kt (duplicate).
