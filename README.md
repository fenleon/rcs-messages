# RCS Messages — RCS for the Light Phone III

RCS messaging as a LightOS tool (`com.lightphone.rcsmessages`), riding the
device's own carrier IMS stack (AOSP single registration / QTI ImsRcsService).
No Google account, no Google Messages, no MicroG.

Status: **spec phase** — feasibility spec lives in the dev workspace
(`light-phone/RCS-LP3-PLAN.md`, section "Carrier path"). UI is bootstrapped
from [fenleon/chats](https://github.com/fenleon/chats) (Kotlin, Light design
system); the RCS client layer is new code.

## Locked decisions

- Built-in Light Messages is stock AOSP MMS (`com.android.mms` priv-app);
  RCS lives only here. No SMS fallback in v1. Light is pursuing LP3 RCS in
  parallel — findings and code flow back to them; the built-in app may gain
  RCS from Light itself later.
- Client UI is always our own Kotlin (LightScreen, chats pattern) — never a
  third-party messaging UI.
- Kotlin for tool and client layer; no SMS fallback in v1.
