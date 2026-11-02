# G1 native Android conversation fix (0.2.5)

Observed on a real Android device: Forge answers a first statement (e.g.,
"my favorite color is purple") but can return "No visible answer was
generated" on following turns, even for "hello".

## Root issue

The pinned upstream Android llama.cpp example has two significant limits for
small Qwen3 models:

- Its native sampling stop position is calculated from a position that already
  includes the user prompt, **then adds the prompt length again**.
- When the model hits a token cap instead of an end-of-generation token, the
  example does not finalize the assistant message in native chat history.

Qwen3 also defaults to thinking mode. On a small model and finite generation
limit it can use the whole allowance without emitting final user-visible text.
The /no_think instruction helps but is a soft switch.

## Fix

The **pinned upstream Android code is untouched in Git**. After cloning it,
`scripts/patch-llama-android.py` applies exactly checked replacements:

1. Prefill Qwen3's supported empty thinking block in the native inference
   context to **hard-disable thinking per turn**. This uses Qwen's published
   inference method, and only runs when the template contains the hybrid
   thinking support.
2. Correct the stop position to start from the current native position.
3. Commit the native assistant turn to history when the output cap is reached,
   preventing subsequent user prompts from continuing an unfinished assistant
   turn.

The existing Kotlin conversation window and output filter remain in place.
The app still runs fully offline, uses the same bundled model and stable
release keystore, and stores no conversations to disk until G2.

### Android acceptance

Test in airplane mode:

- "My favorite color is purple." -> an acknowledgment
- "What color did I just say?" -> purple
- "Hello" -> a normal reply
- "I have a red bicycle." -> acknowledgment
- "What color is my bicycle?" -> red
- "Explain Newton's third law" -> an answer

Inspect diagnostics and report unexpected blank replies. Passing the GitHub
native compile is not a substitute for verifying replies on the phone.

Qwen documentation: https://github.com/QwenLM/Qwen3/blob/main/docs/source/inference/transformers.md
