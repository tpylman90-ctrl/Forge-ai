package com.forgeai.assistant.inference

/**
 * Recent user statements are included as bounded context because small local
 * models can miss details across turns. This is session-only and never saved.
 * llama.cpp's chat template also tracks the conversation in native memory.
 */
internal object SessionPromptBuilder {
    private const val maxPriorUserTurns = 4
    private const val maxTotalContextChars = 600
    private const val maxMessageChars = 220

    fun build(priorUserMessages: List<String>, current: String): String {
        val recent = priorUserMessages.takeLast(maxPriorUserTurns)
        val selected = mutableListOf<String>()
        var used = 0
        for (message in recent.asReversed()) {
            val snippet = message.trim().replace(Regex("\\s+"), " ")
                .take(maxMessageChars)
            if (snippet.isNotBlank() && used + snippet.length <= maxTotalContextChars) {
                selected.add(snippet)
                used += snippet.length
            }
        }

        // Qwen3-0.6B may ignore a system-level /no_think. Repeat for every user turn
        // so a short 256-token mobile response budget is not spent on hidden thinking.
        if (selected.isEmpty()) return "$current\n/no_think"
        return buildString {
            append("Recent statements from the user in this conversation (context only):\n")
            selected.asReversed().forEach { append("- ").append(it).append('\n') }
            append("\nRespond to the latest user message using this context when relevant. ")
            append("Do not claim that information explicitly shared above is unavailable.\n")
            append("Latest user message: ").append(current)
            append("\n/no_think")
        }
    }
}
