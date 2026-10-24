# RCS Messages (`com.lightphone.rcsmessages`) — index + rules only

RCS messaging for the Light Phone III as a LightOS tool. See
`light-phone/RCS-LP3-PLAN.md` §"Carrier path" for the feasibility spec,
phases, and kill criteria; findings/history live in the light-phone
`WORKLOG.md`, not here.

- **The UI is the chats tool's UI, verbatim** (user directive 2026-10-24):
  the whole `tool/src/main/kotlin/com/lightphone/chats/` tree — screens
  (ChatListScreen, ThreadScreen, ComposerScreen, Contacts…), projection store
  (`ThreadRowLogic.kt` ThreadRowValues), Shared components — is copied
  unchanged from `light-phone/chats` (keep that package). Do NOT hand-roll
  alternative screens; adapt chats' files in place.
- **Engine swap plan:** the Matrix layer (`server/MatrixRepository.kt`,
  ChatClient, Trixnity deps) gets replaced by the RCS engine
  (registration + UCE + SipDelegate messaging) behind the same interfaces the
  screens already consume. `LIGHT-FINDINGS.md` documents the access wall.
- Kotlin everywhere; no Google Messages UI/code (Messages APK is RE reference
  only, gitignored `reference/` in the light-phone workspace).
- Battery rule: verbose logging runtime opt-in, off by default.
