package com.example.translator.data

import com.example.translator.model.Language

/**
 * AI 后端提供方。
 *
 * 两种提供方都走 OpenAI 兼容的 `/chat/completions` 接口（含 SSE 流式），
 * 因此共用同一套请求/解析逻辑，区别只在默认地址、默认模型和是否需要 API Key。
 */
enum class AiProvider(
    val displayName: String,
    val defaultBaseUrl: String,
    val defaultModel: String,
    val requiresApiKey: Boolean
) {
    DEEPSEEK(
        displayName = "DeepSeek API",
        defaultBaseUrl = "https://api.deepseek.com/chat/completions",
        defaultModel = "deepseek-chat",
        requiresApiKey = true
    ),
    OLLAMA(
        displayName = "Ollama 本地模型",
        defaultBaseUrl = "http://10.0.2.2:11434/v1/chat/completions",
        defaultModel = "gemma3",
        requiresApiKey = false
    );

    companion object {
        /**
         * 可在 Ollama 里一键拉取的开源权重模型候选。
         * 拉取方式：`ollama pull gemma3`。各模型遵循其各自的开源权重许可，
         * 详情见 https://ollama.com/library 每个模型的 License 说明。
         */
        val OLLAMA_MODEL_SUGGESTIONS = listOf("gemma3", "llama3.2", "qwen2.5")
    }
}

data class AppSettings(
    val provider: AiProvider = AiProvider.DEEPSEEK,
    val apiKey: String = "",
    val baseUrl: String = "",
    val model: String = "",
    val preferredSummaryLanguage: Language = Language.CHINESE
)

data class ApiConfig(
    val apiKey: String,
    val baseUrl: String,
    val model: String
)
