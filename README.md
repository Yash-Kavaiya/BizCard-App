# BizCard AI

AI business card scanner and NFC tap lead manager for Android. Scan cards with CameraX, extract
contact details with Gemini (with an on-device regex fallback), read vCards/URLs from NFC tags and
QR codes, and export leads to CSV, vCard or CRM integrations.

| | |
|---|---|
| Application ID | `com.aistudio.bizcardai.leadtap` (debug builds: `.debug` suffix) |
| Min / target SDK | 24 / 36 |
| Toolchain | AGP 9.1, Gradle 9.3.1 (wrapper), JDK 17, Kotlin 2.2 |

## Local setup

1. Install Android Studio (latest stable) with JDK 17.
2. Copy `.env.example` to `.env` and set your key:
   ```
   GEMINI_API_KEY=your-key
   ```
   Without a key the app still works and uses on-device regex parsing.
3. Optional: put a shared `debug.keystore` in the repo root so every developer's debug build has
   the same signing certificate. It is git-ignored.
4. Open the project in Android Studio, or use the wrapper:

| Task | Command |
|---|---|
| Debug APK | `./gradlew :app:assembleDebug` |
| Lint | `./gradlew :app:lintDebug` |
| Unit + Robolectric tests | `./gradlew :app:testDebugUnitTest` |
| Unit test coverage (JaCoCo) | `./gradlew :app:createDebugUnitTestCoverageReport` |
| Record screenshot baselines | `./gradlew :app:recordRoborazziDebug` |
| Verify screenshots | `./gradlew :app:verifyRoborazziDebug` |
| Instrumented tests (device/emulator) | `./gradlew :app:connectedDebugAndroidTest` |

Screenshot baselines are written to `app/src/test/screenshots/`. Record and commit them once, then
CI can be switched to `verifyRoborazziDebug`.

## Release build configuration

- R8 full mode with code and resource shrinking (`proguard-android-optimize.txt` plus
  `app/proguard-rules.pro`). JSON models use Moshi codegen, so no reflection keep rules are needed.
  `Log.v/d/i` calls are stripped from release builds.
- Version: `-PversionCode=<int> -PversionName=<x.y.z>`. Defaults are `1` / `1.0.0`.
- Signing is read only from environment variables, never from files in the repo:

| Variable | Meaning |
|---|---|
| `KEYSTORE_PATH` | Path to the upload keystore (defaults to `./my-upload-key.jks` if present) |
| `STORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias (default `upload`) |
| `KEY_PASSWORD` | Key password |

If no keystore is found, the release variant is built unsigned. CI uses that to validate R8 on
every pull request.

### Room schema

`AppDatabase` exports its schema to `app/schemas/`. Commit that folder. CI warns and uploads the
generated schema as an artifact if it is missing or changed. Any `version` bump must ship a
`Migration`. Destructive fallback is only enabled for downgrades.

## CI/CD (GitHub Actions)

### `CI` (`.github/workflows/ci.yml`)

Runs on pushes to `main`, pull requests and manual dispatch:

1. Wrapper checksum validation, then lint (the SARIF is uploaded to GitHub code scanning).
2. Unit, Robolectric and Roborazzi tests with a JaCoCo coverage report.
3. `assembleDebug`, `assembleRelease` and `bundleRelease`. The unsigned release still runs R8 and
   `lintVitalRelease`.
4. Instrumented tests (Room DAO and app smoke tests) on an API 34 emulator.

Reports, test results and the debug APK are uploaded as workflow artifacts.

### `Release` (`.github/workflows/release.yml`)

Triggered by pushing a tag such as `v1.2.0` (tags containing `-`, like `v1.2.0-rc.1`, become
pre-releases), or manually with a version name and Play track.

1. versionName comes from the tag or input. versionCode = `VERSION_CODE_OFFSET` + run number, so
   it always increases.
2. Restores the keystore from secrets, runs unit tests, builds a signed AAB and APK, and verifies
   the APK signature with `apksigner`.
3. Uploads the AAB, APK, SHA256SUMS and R8 `mapping.txt` as artifacts. On tags it publishes a
   GitHub Release (without the mapping file).
4. Optionally uploads the AAB and mapping file to Google Play.
5. Always deletes the decoded keystore and `.env` afterwards.

The job runs in the `production` environment. Add required reviewers to that environment in
**Settings → Environments** to gate releases.

```bash
git tag v1.0.0
git push origin v1.0.0
```

### Secrets and variables

Configure these in **Settings → Secrets and variables → Actions**. Environment-scoped secrets on
`production` are recommended.

| Secret | Required | Description |
|---|---|---|
| `KEYSTORE_BASE64` | Yes | Base64 of the upload keystore (`.jks`) |
| `STORE_PASSWORD` | Yes | Keystore password |
| `KEY_ALIAS` | Yes | Key alias |
| `KEY_PASSWORD` | Yes | Key password |
| `GEMINI_API_KEY` | No | Gemini API key baked into the release build |
| `PLAY_SERVICE_ACCOUNT_JSON` | No | Play Console service account JSON (plain text) |

| Variable | Default | Description |
|---|---|---|
| `VERSION_CODE_OFFSET` | `0` | Added to the run number, e.g. to stay above an existing Play versionCode |
| `PLAY_TRACK` | `none` | Track used for tag-triggered releases: `internal`, `alpha`, `beta`, `production` or `none` |
| `PLAY_RELEASE_STATUS` | `draft` | `draft`, `inProgress` or `completed`. Apps that have never been published on Play must use `draft` |

Create an upload keystore and encode it:

```bash
keytool -genkeypair -v -keystore upload-keystore.jks -alias upload \
  -keyalg RSA -keysize 4096 -validity 10000

# macOS / Linux
base64 -i upload-keystore.jks | tr -d '\n' > keystore.b64
# Windows PowerShell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("upload-keystore.jks")) > keystore.b64
```

Paste the contents of `keystore.b64` into `KEYSTORE_BASE64`, then delete the `.b64` file. Keep
the keystore and its passwords in a password manager, and enroll in Play App Signing.

## Security notes

- **Gemini API key.** Any key shipped inside an APK can be extracted. The app sends the key in the
  `x-goog-api-key` header together with `X-Android-Package` and `X-Android-Cert`, so in the
  Google Cloud console you should restrict the key to:
  - **Application restriction: Android apps.** Add package `com.aistudio.bizcardai.leadtap` with
    the **Play App Signing** SHA-1 (Play Console → App integrity), and the upload key SHA-1 for
    sideloaded builds. For debug builds add `com.aistudio.bizcardai.leadtap.debug` with your debug
    SHA-1.
  - **API restriction:** Generative Language API only.

  For stronger protection, move the calls to Firebase AI Logic with App Check. The dependencies
  are already listed (commented out) in `app/build.gradle.kts`.
- Cleartext HTTP is disabled (`network_security_config.xml`), and only system CAs are trusted.
- CRM integration credentials are stored in a separate preferences file that is excluded from
  cloud backup and device-to-device transfer.
- NFC payloads are size-limited and parsed strictly. Only vCard data and `http`/`https` URLs are
  accepted.
- `FileProvider` only exposes `cache/exports/`.
- Demo behaviour (seed leads, simulated NFC tap, sample cards) is compiled into debug builds only.

## Known limitations

- The Kotlin namespace is `com.example`. It is independent of the application ID, so it does not
  affect Play publishing, but it can be renamed later in a dedicated refactor.
- Firebase is not used at runtime. The `google-services` plugin is configured to only warn when
  `app/google-services.json` is missing.
