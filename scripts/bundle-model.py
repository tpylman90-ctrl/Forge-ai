#!/usr/bin/env python3
"""Download, authenticate by Hugging Face's LFS SHA-256, and package the offline test model.

The model is an Apache-2.0 third-party asset, downloaded only in CI.
Do not commit the weights to Git. An imported user model always takes priority.
"""
import hashlib
import os
import pathlib
import shutil

from huggingface_hub import HfApi, hf_hub_download

REPOSITORY = "tensorblock/Qwen_Qwen3-0.6B-GGUF"
FILENAME = "Qwen3-0.6B-Q4_K_M.gguf"
DEST = pathlib.Path("app/src/main/assets")
DEST.mkdir(parents=True, exist_ok=True)

api = HfApi()
info = api.model_info(REPOSITORY, files_metadata=True)
sibling = next((s for s in info.siblings if s.rfilename == FILENAME), None)
if sibling is None or not sibling.lfs or not sibling.lfs.get("sha256"):
    raise RuntimeError("GGUF model missing LFS SHA-256 metadata; refusing unverified bundle")
expected_sha = sibling.lfs["sha256"].lower()
assert len(expected_sha) == 64

revision = os.environ.get("FORGE_MODEL_REVISION", info.sha)
print(f"Model: {REPOSITORY}/{FILENAME} at revision {revision}")
src = pathlib.Path(
    hf_hub_download(repo_id=REPOSITORY, filename=FILENAME, revision=revision)
)
checksum = hashlib.sha256()
size = 0
with src.open("rb") as inp, (DEST / "forge-default.gguf").open("wb") as out:
    while True:
        block = inp.read(1024 * 1024)
        if not block:
            break
        checksum.update(block)
        size += len(block)
        out.write(block)
actual_sha = checksum.hexdigest()
if actual_sha != expected_sha or size < 1_000_000:
    (DEST / "forge-default.gguf").unlink(missing_ok=True)
    raise RuntimeError("Downloaded GGUF checksum or length validation failed")
with (DEST / "forge-default.gguf").open("rb") as inp:
    if inp.read(4) != b"GGUF":
        raise RuntimeError("Model was downloaded, but it is not a GGUF file")
(DEST / "forge-default.sha256").write_text(f"{actual_sha} {size}\n", encoding="utf-8")

# Distribute a copy of the upstream Apache 2.0 license alongside the weights.
license_path = hf_hub_download(repo_id="Qwen/Qwen3-0.6B", filename="LICENSE")
shutil.copyfile(license_path, DEST / "MODEL_LICENSE.txt")
(DEST / "MODEL_ATTRIBUTION.txt").write_text(
    "Forge AI model: Qwen3-0.6B\n"
    "Original publisher: Qwen\n"
    "Quantization: TensorBlock, Q4_K_M\n"
    f"Source: https://huggingface.co/{REPOSITORY}\n"
    "License: Apache License 2.0\n"
    f"Model revision: {revision}\n"
    f"SHA-256: {actual_sha}\n",
    encoding="utf-8"
)
print(f"Bundled GGUF verified: {size:,} bytes, SHA-256 {actual_sha}")
