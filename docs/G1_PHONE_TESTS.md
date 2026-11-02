# G1 bundled model — phone acceptance tests

Use 64-bit ARM Android 13+ with enough free storage. The downloaded artifact should be substantially larger than G1 without weights.

- [ ] Download and install `app-debug.apk`
- [ ] Launch the app and navigate all six screens
- [ ] Let first-launch model extraction finish: observe Setup progress
- [ ] Confirm it shows Qwen3-0.6B Q4_K_M, SHA-256 and byte count
- [ ] Confirm model is automatically loaded, or tap Load if it failed
- [ ] Enable airplane mode and ask a basic question
- [ ] Verify tokens stream while offline
- [ ] Ask another question, tap Stop mid-generation
- [ ] Verify next generation can start after stop
- [ ] Unload and reload without app restart
- [ ] Force-stop and reopen: model should remain installed without needing extraction
- [ ] Test optional replacement via Android document picker after unload
- [ ] Report response speed, heating, free RAM and any status errors

A successful CI build validates packaging only. **No phone inference is verified until these tests pass.** Any reported failures should include the visible status text; avoid including private conversations in shared logs.
