#!/usr/bin/env python3
"""Apply and validate narrow fixes to the pinned llama.cpp Android sample.

The Forge source repository does not vendor large upstream C++ sources; GitHub
Actions clones a pinned upstream revision and applies these targeted edits.
Fail closed if the pinned source no longer matches expected code.
"""
from pathlib import Path

target = Path("vendor/llama.cpp/examples/llama.android/lib/src/main/cpp/ai_chat.cpp")
source = target.read_text(encoding="utf-8")

def replace_once(old: str, new: str, label: str) -> None:
    global source
    found = source.count(old)
    if found != 1:
        raise RuntimeError(f"{label}: expected exactly one source match, found {found}")
    source = source.replace(old, new, 1)

# The original Android sample adds the prompt length twice to the output
# position budget. This also causes truncated responses to skew later turns.
replace_once(
    "    stop_generation_position = current_position + user_prompt_size + n_predict;",
    """
    // Qwen3's GGUF chat template supports thinking control. Its default
    // behavior may spend a short mobile reply budget entirely in <think>.
    // Prefill an empty thinking block, the hard non-thinking method published
    // by Qwen, only when the model's built-in template supports it.
    const auto template_source = common_chat_templates_source(g_chat_templates.get());
    const bool has_qwen_thinking = template_source.find("enable_thinking") != std::string::npos
            && template_source.find("<think>") != std::string::npos;
    if (has_qwen_thinking) {
        const std::string no_think_prefix = "<think>\\n\\n</think>\\n\\n";
        const auto prefill_tokens = common_tokenize(g_context, no_think_prefix, false, true);
        if (decode_tokens_in_batches(g_context, g_batch, prefill_tokens, current_position, true)) {
            LOGe("%s: cannot apply Qwen non-thinking prefix", __func__);
            return 2;
        }
        current_position += (int) prefill_tokens.size();
        // Include the prefill in this assistant turn's native chat history,
        // so subsequent turns are formatted against the actual KV contents.
        assistant_ss << no_think_prefix;
    }
    // Output budget starts AFTER processing the user's prompt and prefill.
    stop_generation_position = current_position + std::max(1, (int) n_predict);""",
    "Qwen non-thinking prefix and token budget",
)

replace_once(
    '''        LOGw("%s: STOP: hitting stop position: %d", __func__, stop_generation_position);
        return nullptr;''',
    '''        LOGw("%s: STOP: hitting stop position: %d", __func__, stop_generation_position);
        // Even when no EOS is generated, commit a completed assistant turn.
        // Otherwise the following user message is appended to an open
        // assistant continuation and future responses may be blank.
        chat_add_and_format(ROLE_ASSISTANT, assistant_ss.str());
        return nullptr;''',
    "Close assistant turn at the output limit",
)

target.write_text(source, encoding="utf-8")
assert source.count('chat_add_and_format(ROLE_ASSISTANT, assistant_ss.str());') == 2
assert 'const bool has_chat_template = common_chat_templates_was_explicit' in source
assert 'stop_generation_position = current_position + std::max(1, (int) n_predict);' in source
print("Forge Android native multi-turn patch applied and validated.")
