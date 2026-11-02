# Bundled model for G1

Forge AI includes an offline model by default starting with version 0.2.1 G1 bundled.

- Model: Qwen3-0.6B Q4_K_M GGUF
- Original publisher: Qwen
- Quantization: TensorBlock
- Source: https://huggingface.co/tensorblock/Qwen_Qwen3-0.6B-GGUF
- Original model: https://huggingface.co/Qwen/Qwen3-0.6B
- Model license: Apache 2.0
- Approximately 484 MB; the installable APK is much larger than G0/G1
- The Apache 2.0 license and source attribution are added to assets during CI packaging

The publicly visible GitHub repository **never stores model weights**. The Actions workflow calls `scripts/bundle-model.py`, obtains the selected GGUF from Hugging Face, checks its LFS SHA-256 and GGUF header, bundles the weights and attribution, then builds the APK. A model SHA-256 and byte count are included in the APK. Forge checks those during first-launch extraction to app-private storage.

If the user previously imported a model into Forge, **that model is preserved** and takes priority over the built-in one. A fresh install copies the verified default model to private app storage and attempts to load it.

No network access is required at runtime. About 0.5 GB additional free disk space is required for first-launch extraction, beyond the APK's installed size. The actual available RAM and performance must be measured on the phone.

If distributing further, check and retain the license and original model attribution. This is a local assistant test model, not an automatic update to the model's learned parameters.

No model file should ever be pushed to the public Git repository.
