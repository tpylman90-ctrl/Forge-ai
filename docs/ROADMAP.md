# Forge AI — implementation gates

## G0: verified CI
- Six navigable Compose screens and dark theme
- Public source with no personal data
- GitHub Actions produced a debug APK

## G1: local intelligence (requires phone verification)
- Official llama.cpp Android JNI built from pinned revision
- Arm64 CPU-only 64-bit APK, Android 13+
- Document picker GGUF import, header validation, copy to app-private storage
- SHA-256 checksum on import; temporary file removed if import fails
- Load/unload model, system prompt, streamed local tokens, stop generation
- Diagnostics page and state/error handling; no internet permission
- **Acceptance:** import Qwen3-0.6B Q4 GGUF, load, converse in airplane mode, stop mid-response, recover from invalid model selection without losing a valid one
- **Limit:** responses are ephemeral; persistent conversation storage comes in G2

## G2: conversation persistence
- Room SQLite, threads, durable history, edit/delete, session recovery

## G3: opt-in long-term memory
- User-approved memory store and retrieval

## G4: stabilization and privacy hardening
- Performance/thermal metrics, crash and airplane-mode testing; backup/export and delete controls

## Later
- Voice, Android tools, assistant-role support, multi-step tasks and tested model updates

Never mark a milestone verified merely because code was committed.
