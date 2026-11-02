# G1 follow-up question regression

G1.1 improves compact-model multi-turn reliability without introducing persistent memory.

## Root causes found
- The prior system prompt said it could not access personal information, even facts provided directly in the current conversation.
- Qwen3's non-thinking instruction was placed in the system prompt only, which can be ignored on individual turns.
- The response filter correctly hides thought text; when the model used up the token budget thinking, it produced no visible answer.

## Changes
- The system prompt explicitly permits using voluntarily shared details within the current conversation.
- Every user inference turn ends with Qwen3's `/no_think` switch, reducing hidden reasoning output.
- The prompt includes a bounded reminder of up to four recent user messages (maximum 600 total characters).
- Native llama.cpp conversation handling remains intact. No chat content is uploaded or saved to disk.

## Test procedure
1. Install the latest **signed** APK as an update (same signing key).
2. Enable airplane mode.
3. Send `My favorite color is purple.` and confirm Forge acknowledges the statement.
4. Send `What color did I say?` and verify `purple` is present in the reply.
5. Send `I have a red bicycle.` and follow with `What color is the bicycle?`.
6. Ask a basic general-knowledge question, then another follow-up.
7. Confirm no `<think>` tags or hidden reasoning is displayed.
8. Close/reopen the app. Session memory still does not persist after process exit; persistent conversations are G2.

The build and unit tests can confirm Kotlin and prompt formatting. On-device
answers are probabilistic; G1.1 is only verified after the phone reproduces
the desired two-turn behavior.
