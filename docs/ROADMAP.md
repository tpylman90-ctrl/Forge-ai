# Forge AI — Implementation Gates

This document distinguishes shipped behavior from future plans.

## G0 — Android foundation
- Six navigable Compose screens
- Dark interface
- Public-repo-safe project and ignored local data
- GitHub Actions debug APK job
- No runtime permissions
- **Acceptance:** Gradle compiles an installable APK; all screens open and switch cleanly.

## G1 — Offline inference
- Evaluate llama.cpp and Qwen3-0.6B GGUF (not bundled)
- Import and checksum model into app-private storage via user-selected document
- Explicit load/unload, streaming generation, cancel, diagnostics
- No internet dependency for inference

## G2 — Conversation persistence
- Room SQLite database, thread history, deletion, durable storage
- UI and performance tests

## G3 — Opt-in long-term memory
- Separate typed approved-memory store, editable and deletable
- Context retrieval scoped to user preferences and current task

## G4 — Release hardening
- Performance and crash testing, airplane-mode tests
- Backup exclusion, export/deletion controls, secure storage review

## Later (M2 onward)
- Push-to-talk offline voice, permission-controlled Android actions,
  assistant-role support, planning and evaluated model updates

**Guardrail:** A mock interface is not an AI model. Each gate must pass device tests
before status is upgraded.
