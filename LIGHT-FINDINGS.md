# RCS on the Light Phone III — findings for Light

From independent feasibility work on an LP3 (TLP301, `00WW_1_440000`, security
patch 2025-03-01), October 2026. Full probes and a proven Kotlin client layer:
[fenleon/rcs-messages](https://github.com/fenleon/rcs-messages). We know Light
is pursuing LP3 RCS — this is everything we measured, offered back.

## 1. Hardware attestation: the LP3 has no chain — at all

- Attested keygen in KeyMint fails: `ATTESTATION_KEYS_NOT_PROVISIONED`
  (keystore2 error −74). The device is RKP-only and Google's remote key
  provisioning returns HTTP 444 (`OUT_OF_KEYS_PERMANENT_ERROR`) — device online
  and validated on Telekom LTE, so this is a server-side refusal, not
  connectivity.
- Non-attested keygen works (TEE healthy); the resulting "cert" is the AOSP
  software placeholder (`CN=Fake`, self-signed).
- **Consequence: Google-device-integrity gates (Play Integrity, Google RCS
  provisioning) can never pass on the LP3 — there is no attestation chain in
  any flavor, not even the emulator's "unregistered" one.** Any RCS path must
  avoid Google's device checks. (Reference: on google_apis emulator images the
  chain roots at `Droid Unregistered Device CA`; on Play-certified images at
  Google's *software* attestation root — Google-signed software chains pass.)

## 2. The carrier-RCS stack is fully present — and wakes up

- LP3 ships the complete Qualcomm IMS stack: `org.codeaurora.ims` (MMTel/VoLTE,
  device-default ImsService), `vendor.qti.imsrcs` (`/system_ext/app/
  ImsRcsService`, RCS ImsService), `imsdatachannel`, UCE shim,
  `imsserviceentitlement`.
- Registered on Telekom DE 262-01 (fraenk): LTE, VoLTE (`mVopsSupport=2`).
- Carrier config: `carrier_volte_available_bool=true`,
  `carrier_rcs_provisioning_required_bool=true`,
  `config_ims_rcs_package_override_string=null`,
  `carrier_certificate_string_array=[]` (no carrier certificates),
  entitlement URLs empty (out-of-band provisioning).
- **5-command shell sequence lights the whole stack up** (feature 2 = RCS on
  this build):

  ```
  cmd phone src set-test-enabled true
  cmd phone src set-device-enabled true
  cmd phone src set-feature-validation -s 0 true
  cmd phone ims set-ims-service -s 0 -d -f 2 vendor.qti.imsrcs
  cmd phone src set-carrier-enabled -s 0 true
  ```

  Result: `ImsResolver` binds `vendor.qti.imsrcs/.ImsRcsService` on both slots,
  `ImsRcsFeatureImpl onFeatureReady`, `notifyImsFeatureStatus feature=RCS
  status=READY`, modem `imsd` emits `onRcsServiceStatusUpdate`; IMS
  registration = REGISTERED (single registration rides VoLTE). All overrides
  are ephemeral (lost on reboot) — `set-carrier-enabled` is the step that
  bypasses `carrier_rcs_provisioning_required_bool`.

## 3. Client access: exact permission/role map

Measured with a reflection probe (`probe/` in the repo) as (a) normal app on
the LP3, (b) same on google_apis emulator, (c) priv-app + whitelist, (d)
platform-signed (AOSP testkey) priv-app, (e) SMS-role holder:

| Surface | Permission | Protection | Reachable by |
|---|---|---|---|
| `ImsManager.getImsRcsManager(sub)` | none | — | any app |
| `ImsRcsManager.getRegistrationState` | `READ_PRIVILEGED_PHONE_STATE` | signature\|privileged | priv-app (+ whitelist) ✓ measured |
| `RcsUceAdapter.requestCapabilities` (UCE) | `ACCESS_RCS_USER_CAPABILITY_EXCHANGE` | **INTERNAL** | platform-signed system app or role holder only |
| `ProvisioningManager.isRcsVolteSingleRegistrationCapable` | `PERFORM_IMS_SINGLE_REGISTRATION` **or carrier privileges** | INTERNAL | same |
| `SipDelegateManager.createSipDelegate` | role/privilege gated | — | not reachable sideloaded |

- Carrier-privilege door is closed on this SIM (empty config cert list; stock
  Google Messages installed-not-launched → `has-carrier-privileges` = false;
  Messages itself gates on `hasCarrierPrivileges()` and uses Jibe cloud on
  Telekom DE, not carrier IMS).
- The SMS role does **not** carry UCE on the LP3's PermissionController
  (probe held `role.SMS`, still denied).
- **`android.app.role.SYSTEM_SHELL`** (Light's PermissionController; static,
  systemOnly, default holder `com.android.shell`) carries exactly
  `ACCESS_RCS_USER_CAPABILITY_EXCHANGE` + `PERFORM_IMS_SINGLE_REGISTRATION`.
  A systemOnly role is the natural door for a first-party or approved RCS
  client.

**Bottom line: a sideloaded app cannot reach the RCS binder. Every working
door is one Light controls: platform-signed `com.lightos` integration, the
SYSTEM_SHELL role, or a carrier-config change.**

## 4. What we can hand over

- **A proven Kotlin client layer** up to the permission boundary:
  registration-state callbacks (live-tested as priv-app on emulator), UCE
  adapter usage, SipDelegate surface mapping, enablement sequencing —
  `probe/` in the repo (MIT).
- **The RCS Messages UI**: our Light-design-system messaging client
  (bootstrapped from the chats tool) with the RCS engine pluggable behind it —
  ready to be pointed at the QTI ImsRcsService from inside `com.lightos` or a
  Light-signed tool.
- **An open integration question**: persistent enablement. Shell overrides do
  not survive reboot. **Measured: `ProvisioningManager.setRcsProvisioningStatusForCapability`
  is denied even to a platform-signed priv-app — provisioning writes require
  carrier privileges specifically.** On a Light-controlled build the remaining
  paths are a build-level enablement patch, carrier config, or SIM ARA-M
  provisioning — all Light/Telekom side.

## 5. Suggested next steps together

1. Light: decide the integration point (com.lightos native module vs
   SYSTEM_SHELL-role tool vs carrier-config change).
2. We hand over the client layer + UI code (repo is public, MIT).
3. Joint live test of UCE capability exchange + messaging against Telekom IMS
   (needs the door above; the fake-IMS harness we built for engine testing is
   also available).
