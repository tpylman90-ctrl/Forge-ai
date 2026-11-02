# G1 Android acceptance checklist

Tester: use an ARM64 Android 13+ phone and a small Q4 GGUF model.

- [ ] Install G1 APK over G0 and launch without crashing
- [ ] Navigate all 6 screens
- [ ] Import GGUF using Android file picker (no storage permission)
- [ ] Confirm filename, byte size and SHA-256 reported
- [ ] Try wrong file: rejected, previous valid GGUF remains available
- [ ] Load model, observe correct state and RAM use
- [ ] Turn on airplane mode, ask a factual question, see streamed tokens
- [ ] Ask long question, tap Stop, then send a new message
- [ ] Unload and load again without restarting
- [ ] Force-stop/restart app; imported GGUF remains available
- [ ] Confirm no chat history is claimed to be saved (G2)
- [ ] Report time to first token, seconds for response, heating and battery effects

If loading fails, capture the **status text** and a redacted logcat. Do not share full personal conversations.
