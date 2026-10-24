# RCS Messages — RCS for the Light Phone III

RCS messaging as a LightOS tool (`com.lightphone.rcsmessages`), riding the
device's own carrier IMS stack (AOSP single registration / QTI ImsRcsService).
No Google account, no Google Messages, no MicroG.

Status: **spec phase** — feasibility spec lives in the dev workspace
(`light-phone/RCS-LP3-PLAN.md`, section "Carrier path"). UI is bootstrapped
from [fenleon/chats](https://github.com/fenleon/chats) (Kotlin, Light design
system); the RCS client layer is new code.

## Layout

- `tool/` — the LightOS tool (single module, tasks/passes pattern:
  `lighttool.toml` + `lighttool` plugin + LightScreen UI). Entry screen:
  `HomeScreen.kt`. Builds against the sibling `light-phone/light-sdk` checkout
  (included build); `serverPackage = "com.lightos"` on a real LP3, flip to
  `com.thelightphone.sdk.emulator` for emulator testing. Build:
  `./gradlew :tool:assembleDebug` (workspace JDK21/SDK env from
  `light-phone/tools/env.sh`).
- `probe/` — the C0 access-probe APK (standalone gradle project, not a tool).

## Locked decisions

- Built-in **Phone** tool = React Native inside the `com.lightos` system app
  (klinker android-smsmms native module, SMS/MMS only — code-verified, no RCS).
  RCS lives only here. No SMS fallback in v1. Light is pursuing LP3 RCS in
  parallel — findings and code flow back to them; if Light adds RCS it will be
  a native client inside `com.lightos` (same layer we build).
- Client UI is always our own Kotlin (LightScreen, chats pattern) — never a
  third-party messaging UI.
- Kotlin for tool and client layer; no SMS fallback in v1.
