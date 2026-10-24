# RCS Messages (`com.lightphone.rcsmessages`) — index + rules only

See `light-phone/AGENTS.md` (workspace governance) and
`light-phone/RCS-LP3-PLAN.md` §"Carrier path" for the feasibility spec,
phases, and kill criteria.

- UI bootstrapped from `chats/` (Light design system only); engine layer talks
  to the AOSP IMS single-registration surface. Findings/history go in the
  light-phone `WORKLOG.md`, not here.
- No Google Messages UI/code; Google Messages APK is RE reference only
  (`light-phone/reference/rcs-research/`, gitignored).
- Kotlin everywhere. Battery rule: verbose logging runtime opt-in, off by
  default.
