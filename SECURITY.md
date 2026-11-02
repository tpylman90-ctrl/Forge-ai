# Security and privacy

Forge AI is a public source repository, not a public memory store.

Do not commit personal conversations, imported models, tokens, API keys,
personal files, keystores, exported SQLite databases, or logs containing user
content. The .gitignore file is only a safeguard; review staged files before
committing. Debug APKs are development artifacts, not signed production releases.

Current G0 Android manifest requests no runtime or internet permissions.
Future permissions must be justified, optional when possible, and surfaced
to users before use. Sensitive local data should be held in app-private
storage and excluded from backups where appropriate.

Report security issues privately to the repository maintainer.
