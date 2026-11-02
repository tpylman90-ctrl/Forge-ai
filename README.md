# Forge AI

Offline-first Android personal AI assistant. Public repo for source code only: **never commit** personal data or model weights.

## Project status
- G0: verified Kotlin/Jetpack Compose six-screen Android shell
- G1 bundled: local llama.cpp inference, built-in Qwen3-0.6B Q4_K_M GGUF, streaming chat and diagnostics; APK build will verify packaging
- G2 onward: persistent conversation storage, long-term memory, voice and controlled Android actions planned

## Download APK
Open [GitHub Actions](https://github.com/tpylman90-ctrl/Forge-ai/actions/workflows/android-debug.yml), select the most recent **successful** run, download the APK artifact ZIP, extract `app-debug.apk` and install it on a 64-bit ARM Android 13+ device.

Starting with G1 bundled, the APK contains the Qwen3-0.6B GGUF (about 484 MB). No separate model download is required. On first launch Forge extracts it to app-private storage, validates its SHA-256, then attempts to load it into memory. Device storage and RAM must still be sufficient. Model load success and actual responses require phone testing. The GGUF model can be manually replaced with another model later.

## Build locally
Requires Java 17, Gradle 9.6, SDK API 37.0, NDK 29.0.13113456, CMake 3.31.6, Python 3, and network access **only at build time**:
```sh
bash scripts/setup-llama.sh
python -m pip install "huggingface_hub>=0.29,<2"
python scripts/bundle-model.py
gradle :app:assembleDebug
```

See [model attribution and packaging](docs/BUNDLED_MODEL.md) and [device acceptance tests](docs/G1_PHONE_TESTS.md). The pinned llama.cpp Android library is sourced under its applicable license. The model is Apache-2.0 and its license text and attribution are included in the APK.

## Privacy
No INTERNET permission is required for offline chat. The installed model and its checksums live in Android private app storage, not GitHub. Conversation text is held only in memory until G2. Never upload private data, keys, signing credentials or GGUF weights into this repository.
