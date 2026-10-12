## AItranslater (Android)

一个基于 Jetpack Compose 的三语（简体中文 / English / Français）翻译应用，界面风格参考谷歌翻译。用户在任意一个输入框录入内容后，会自动调用 AI Chat API，以流式方式把译文实时填充到另外两个输入框。

支持两种 AI 后端：**DeepSeek API**（云端）和 **Ollama 本地开源权重模型**（如 Gemma、Llama、Qwen），在 App 内设置页一键切换。

### 功能亮点
1. **三输入框对等交互**：任意语言输入都会成为源语言，另外两种语言由“开始翻译”按钮触发并即时刷新。
2. **流式翻译**：基于 SSE 逐字接收内容，界面实时滚动更新，接近网页端体验。
3. **双 AI 后端**：DeepSeek 云端 API，或本机 Ollama 运行的开源权重模型（OpenAI 兼容接口），设置页可切换。
4. **Jetpack Compose UI**：Material 3 + 响应式设计，状态管理集中在 `TranslatorViewModel`。
5. **首次配置引导**：首次打开若未配置 API Key（DeepSeek 模式），会自动弹出设置；保存后仅保存在当前设备，后续直接打开即可使用。Ollama 模式无需 API Key。
6. **设置面板**：通过右上角“设置”按钮可切换 AI 提供方、维护 API Key / Base URL / 模型，并选择 AI “碎碎念”的偏好语言。
7. **对话碎碎念模式**：底部按钮可进入对话记录面板，自动保存每次翻译并生成 300 字内的语境理解，可随时新建/退出。
8. **错误提示与取消**：网络/接口异常会在界面底部提示，重新输入或重新请求即可重试。

### 目录结构
```
app/
 └─ src/main/java/com/example/translator
     ├─ MainActivity.kt                # Compose 界面（含设置对话框）
     ├─ data/
     │   ├─ AppSettings.kt             # AiProvider 枚举与设置模型
     │   ├─ ChatCompletionsApi.kt      # 封装 OpenAI 兼容 SSE 调用
     │   ├─ SettingsRepository.kt      # DataStore 设置持久化
     │   └─ TranslationRepository.kt   # 多语言翻译协程流
     ├─ model/Language.kt
     └─ ui/
         ├─ TranslatorUiState.kt
         ├─ TranslatorViewModel.kt
         └─ theme/…                    # 主题定义
```

### AI 后端说明

| 提供方 | 接口 | 默认模型 | 说明 |
|---|---|---|---|
| DeepSeek API | `POST https://api.deepseek.com/chat/completions` | `deepseek-chat` | 需要 API Key（在 App 内设置页填写，仅保存在本机） |
| Ollama 本地模型 | `POST <ollama-host>/v1/chat/completions`（OpenAI 兼容） | `gemma3` | 无需 API Key；模型在**本机**运行，数据不出设备 |

两种后端共用同一套 `/chat/completions` 请求与 SSE 解析逻辑（`stream=true`，监听 `data:` 行直至 `[DONE]`）。

#### 使用 Ollama 开源权重模型
1. 在电脑上安装 [Ollama](https://ollama.com)，拉取模型并启动服务：
   ```bash
   ollama pull gemma3
   ollama serve
   ```
2. 在 App 设置页把「AI 提供方」切换为「Ollama 本地模型」。Base URL 与模型留空即用默认值。
3. 网络地址说明：Android 模拟器访问宿主机请用 `http://10.0.2.2:11434/v1/chat/completions`（已设为默认值）；真机请把 `10.0.2.2` 换成电脑的局域网 IP，并在设置页的 Base URL 里修改。
4. 其他可选的开源权重模型：`llama3.2`、`qwen2.5`（同样先 `ollama pull`，再在设置页的 Model 里填写）。

#### 模型与许可说明
- 本 App 代码以 **MIT License** 开源（见仓库根目录 `LICENSE`）。
- 经由 Ollama 运行的第三方模型（如 Gemma、Llama、Qwen）为**开源权重模型**，各模型遵循其各自的权重许可（例如 Gemma 使用条款、Llama 社区许可），详情见 [Ollama 模型库](https://ollama.com/library)中每个模型的 License 说明。拉取并使用这些模型即表示你接受其许可条款。
- DeepSeek API 为第三方云服务，受其服务条款约束。

### DeepSeek API 使用方式
- 接口：`POST https://api.deepseek.com/chat/completions`
- 模型：`deepseek-chat`
- 请求体包含 system + user message（见 `ChatCompletionsApi`），并设置 `stream=true`。
- SSE 监听 `data:` 行，直至 `[DONE]`。
- 运行时优先使用 App 内“设置”里保存的 API Key / Base URL。
- 若安装包内带了默认配置，可作为开发或内部测试兜底；正式使用仍建议在 App 内保存自己的 API Key。
- `local.properties` / 环境变量仍可用于开发阶段生成默认构建值，但不再是用户使用 App 的前提。

> **安全提示**：公共仓库默认不提交任何密钥。最终用户应在 App 内完成本机配置；开发者可在本地构建配置或 CI/CD 私密变量中注入默认值。

### 本地配置
Android Studio 生成的 `local.properties` 通常已经包含 `sdk.dir`。在保留该行的前提下，可追加：

```properties
sdk.dir=/path/to/android-sdk
deepseek.apiKey=your_api_key
deepseek.baseUrl=https://api.deepseek.com/chat/completions
deepseek.model=deepseek-chat
```

### 开发 & 运行
1. 确认已安装 **Android Studio Iguana+**（或 `./gradlew`，JDK 17）。
2. 编译：`./gradlew assembleDebug`（首次运行需联网下载依赖）。
3. 安装：`./gradlew installDebug` 或使用 Android Studio 直接运行。
4. 首次打开若未配置 API Key，App 会自动弹出设置面板；保存后本机后续可直接使用。
5. 连接真机或 Emulator（Android 8.0 / API 26 及以上）。

### 已知限制
- DeepSeek API 需要公网访问；离线环境下无法完成翻译（可切换到本机 Ollama 模式离线使用）。
- 公共仓库默认不内置 API Key，但可在 App 内完成首次配置，无需重新打包。
- 未实现翻译历史、语音输入等扩展功能。

### 许可证
本项目以 MIT License 开源，详见 [LICENSE](LICENSE)。

### 相关文件
- 产品/使用说明：`README.md`（当前文件）
- 后续计划：`update.txt`
