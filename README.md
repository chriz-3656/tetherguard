# TetherGuard Android Security Control Center

**TetherGuard** is a compact Android security control center designed for physical laptop tamper protection and real-time remote incident response. Paired with a host daemon running on a laptop (macOS/Linux/Windows), TetherGuard monitors physical attack surfaces (unauthorized USB devices, laptop lid openings, keystroke injection dongles, perimeter tampering) and provides immediate, authenticated countermeasures.

---

## 🛡️ Architecture & Principles

```
  +----------------------+                     +---------------------------+
  |  TetherGuard Laptop  |                     |  TetherGuard Android App  |
  |     Host Daemon      |                     |      Companion Center     |
  +----------+-----------+                     +-------------+-------------+
             |                                               |
             | WSS (TLS)                                     | WSS (TLS)
             +-----------------> [ Relay Server ] <----------+
                               (or Local Simulator)
```

1. **Clean MVVM Architecture**: Separates UI composables from background networking, local Room database persistence, and cryptographic operations.
2. **Android Keystore Hardware Backing**: Session tokens and shared credentials are encrypted with AES-256-GCM using hardware-backed Android Keystore.
3. **No Blind Trust**: Incoming incident frames are validated against the paired `device_id` and schema.
4. **Replay & Tamper Protection**: Remote commands (`LOCK`, `SHUTDOWN`, `GUARDIAN_ON`, `GUARDIAN_OFF`) include an ISO timestamp, a UUID `request_id`, and an HMAC-SHA256 signature.
5. **Anti-Flood & Accidental Shutdown Guard**: Critical operations like `SHUTDOWN` enforce confirmation dialogs and an automatic 10-second security cooldown.
6. **Graceful Reconnection**: Exponential backoff reconnects to the relay without dropping local state. Shows true `OFFLINE` status when disconnected.

---

## 📱 Primary Screens & Workflows

### 1. Dashboard
- **Monitored Workstation**: Displays device ID, laptop name, and live connection status (`Connected` / `OFFLINE` / `Connecting`).
- **Guardian Status**: High-contrast indicator (`ACTIVE`, `STANDBY`, `TRIGGERED`).
- **Subsystem Telemetry**: Real-time checklist for USB Monitoring, Input Device Watch, Webcam Tamper Vision, and Lid Angle Sensor.
- **Controls**: One-touch `ENABLE GUARDIAN` and `STANDBY`.
- **Emergency Quick Actions**: Authenticated `LOCK` and safety-guarded `SHUTDOWN`.
- **Incident Summary**: Displays the most recent physical event with shortcut to full audit history.
- **Hackathon Demo Panel**: One-tap simulation for USB insertions, lid open events, and keylogger detections.

### 2. QR Pairing
- Live camera viewfinder powered by CameraX with real-time ZXing barcode analysis.
- Validates payload structure (`device_id`, `public_key`, `nonce`, `relay_endpoint`, `secret_token`).
- Hackathon quick-pair presets (`CHRIZ-LAPTOP`, `THINKPAD-X1`) and manual JSON input for testing on emulators without a physical camera.

### 3. Incident Alert
- Immediate high-visibility dialog displaying the alert type, exact timestamp, device info (Vendor ID / Product ID), and captured evidence image.
- Neutral security wording adhering to incident response standards.
- Actions: `[ KEEP LOCKED ]` and `[ REMOTE SHUTDOWN ]`.

### 4. Incident History & Audit Trail
- Chronological list of recorded security incidents stored locally in Room Database.
- Filter chips: `ALL`, `CRITICAL`, `USB`, `PHYSICAL`.
- Detailed inspect sheet showing remediation status (`Workstation Locked`, `Workstation Terminated`, `Event Acknowledged`).

### 5. Security Settings
- Paired workstation identity & public key fingerprint.
- Configurable Relay WebSocket endpoint with auto-reconnect toggle.
- Haptic alert vibration & audio alarm settings.
- Threat model documentation and instant `UNPAIR & WIPE KEYS` option.

---

## 📡 Message Protocol Specification

### 1. Incoming Incident (`INCIDENT`)
```json
{
  "type": "INCIDENT",
  "event": "USB_INSERT",
  "device_id": "TG-8842",
  "timestamp": "2026-09-25T14:32:07Z",
  "severity": "HIGH",
  "metadata": {
    "device_name": "USB Mass Storage (SanDisk Ultra)",
    "vendor_id": "0x0781",
    "product_id": "0x5583",
    "subsystem": "IOKit / udev-guard",
    "threat_score": 78
  },
  "evidence": {
    "available": true,
    "image": "<base64_encoded_frame>"
  }
}
```

### 2. Outgoing Authenticated Command (`COMMAND`)
```json
{
  "type": "COMMAND",
  "command": "LOCK",
  "device_id": "TG-8842",
  "request_id": "b3f29b41-9a74-4b53-bce8-1c4b81c2f9d0",
  "signature": "hmac_sha256_base64_digest",
  "timestamp": 1790325127000
}
```
*Supported Commands*: `GUARDIAN_ON`, `GUARDIAN_OFF`, `LOCK`, `SHUTDOWN`, `PING`.

### 3. Command Acknowledgement (`ACK`)
```json
{
  "type": "ACK",
  "request_id": "b3f29b41-9a74-4b53-bce8-1c4b81c2f9d0",
  "device_id": "TG-8842",
  "status": "SUCCESS",
  "message": "Workstation locked successfully",
  "timestamp": 1790325127350
}
```

### 4. Telemetry Heartbeat (`STATUS`)
```json
{
  "type": "STATUS",
  "device_id": "TG-8842",
  "guardian_state": "ACTIVE",
  "usb_monitoring": true,
  "input_monitoring": true,
  "webcam_watch": true,
  "lid_sensor": true,
  "workstation_locked": false,
  "battery_pct": 92,
  "timestamp": "2026-09-25T14:32:00Z"
}
```

---

## 🔒 Threat Model & Security Posture

| Threat | Mitigation in TetherGuard |
|---|---|
| **Eavesdropping on Relay** | Strict WSS TLS 1.3 encryption across all communication links. |
| **Credential Extraction from Stolen Phone** | Pairing tokens are stored in private SharedPreferences encrypted with AES-256-GCM via Android Keystore. |
| **Command Injection / Replay Attacks** | Every command contains millisecond timestamps, UUID `request_id`, and HMAC-SHA256 signature generated with shared secret. |
| **Unauthorized Laptop Impersonation** | WebSocket frames from unknown `device_id` values are rejected immediately. |
| **Accidental or Malicious Shutdown Floods** | Confirmation modal + client-side 10-second anti-flood cooldown timer. |
| **Arbitrary Code Execution Risk** | Client and host communicate strictly via typed protocol commands (`LOCK`, `SHUTDOWN`, `GUARDIAN_ON/OFF`). No shell commands are accepted. |

---

## 🧪 Testing

Run Robolectric & Local JVM Unit Tests:
```sh
gradle :app:testDebugUnitTest
```
Covered tests:
- `testValidQrPayloadParsing`: Verifies JSON parsing and schema validation.
- `testInvalidQrPayloadRejected`: Confirms invalid schemes, missing device IDs, and empty inputs fail safely.
- `testDeviceIdentityValidation`: Ensures mismatched device IDs are rejected.
- `testAuthenticatedCommandConstruction`: Validates signature and UUID request generation.
- `testShutdownCooldownPrevention`: Verifies duplicate shutdown commands are blocked.
- `testIncidentEntityConversion`: Validates lossless domain-to-entity Room persistence.
- `testSecureStorageUnpairWipesCredentials`: Verifies cryptographic keys are wiped on unpair.
