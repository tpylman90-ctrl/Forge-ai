# Forge AI

Offline-first Android personal AI assistant. This public repo contains source code and build tooling only. Personal chat data and GGUF model weights must never be checked in.

## Status
- **G0: verified** — Kotlin/Compose 6-screen shell and GitHub Actions APK build.
- **G1: implementation** — pinned official llama.cpp Android JNI and local GGUF import, model load/unload, streamed chat, stop and diagnostics.
- **G2 onward: not implemented** — persistent conversations, long-term memory, voice and Android system actions.

## Building from source
G1 uses the upstream official llama.cpp Android engine at commit `3f8af15a76ac8675812e856da45454bafc90f12c`. It is downloaded as ignored vendor source at build time.

1. Install JDK 17, Android SDK API 37, NDK 29.0.13113456, CMake 3.31.6, and Gradle 9.6.
2. Run `bash scripts/setup-llama.sh` at the repo root.
3. Run `gradle :app:assembleDebug`.

Alternatively, use GitHub Actions → Build Android debug APK → successful run → Artifacts.

## Using G1
Find a small, legitimate GGUF model from its publisher (start with Qwen3-0.6B Q4). Save the GGUF file in Downloads. Open Forge → Setup → Import GGUF → Load model → Chat. Import copies the file into private app storage and calculates SHA-256. Model inference is offline, with no INTERNET permission in the manifest. On first load Android needs time and memory for the model. Try airplane mode after loading.

This version deliberately supports ARM64 devices running Android 13+ while evaluating real performance. It does not include a built-in model download flow.

**Caveat:** A successful CI compile verifies packaging, not real-device model compatibility or tokens-per-second. This milestone is complete only after on-device testing of a real GGUF.

## Privacy
No user memories, conversations, tokens, keystores, models, or exports belong in this public source repository. G1 chat is in-memory only; persistent chat and long-term memory are separate milestones.
