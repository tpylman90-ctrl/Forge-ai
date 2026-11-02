package com.forgeai.assistant.inference

/**
 * Converts an incrementally streamed model response into user-visible text.
 *
 * Qwen3 may emit <think>...</think> blocks, including tags split across tokens.
 * Do not expose either the tags or the hidden reasoning in the chat UI.
 * The raw response is intentionally not retained or logged by this class.
 */
internal class AnswerStreamFilter {
    private val opening = "<think>"
    private val closing = "</think>"
    private val pending = StringBuilder()
    private val visible = StringBuilder()
    private var inThought = false

    fun append(piece: String): String {
        pending.append(piece)
        while (pending.isNotEmpty()) {
            val marker = if (inThought) closing else opening
            val position = pending.indexOf(marker)
            if (position >= 0) {
                if (!inThought) visible.append(pending.substring(0, position))
                pending.delete(0, position + marker.length)
                inThought = !inThought
                continue
            }

            // Keep possible partial tag suffixes until the following token.
            val keep = (marker.length - 1 downTo 1).firstOrNull {
                pending.endsWith(marker.substring(0, it))
            } ?: 0
            val flush = pending.length - keep
            if (!inThought && flush > 0) {
                visible.append(pending.substring(0, flush))
            }
            pending.delete(0, flush)
            break
        }
        return visible.toString()
    }

    fun finish(): String {
        // An incomplete hidden reasoning block must never be displayed.
        if (!inThought) visible.append(pending)
        pending.clear()
        return visible.toString().trim()
    }
}
