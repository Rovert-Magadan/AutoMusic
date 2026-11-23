# AutoMusic v0.1 — Android 12 / TS18A prototype

Kotlin + Jetpack Compose. Landscape, optimized for 1280x720.

## Build on GitHub Actions (Windows PC, no Android Studio required)
1. Sign in to GitHub and create a **private** repository, e.g. `AutoMusic` (do not initialize with README).
2. Extract this ZIP. Upload the **contents** of the `AutoMusic` directory to the repository (including `.github/workflows/build-apk.yml`; Windows Explorer may hide `.github`). Commit to `main`.
3. Open the repository's **Actions** tab, choose **Build AutoMusic APK**, then **Run workflow**.
4. Wait for a green build. Open the run and download the artifact `AutoMusic-v0.1-debug-APK`.
5. Unzip the artifact to get `app-debug.apk`, copy to USB and install on the TS18A head unit.

If the workflow is not listed, verify `.github/workflows/build-apk.yml` was uploaded and Actions are enabled.

## Build in Android Studio
Open the `AutoMusic` folder as a project; use JDK 17 and Android SDK 35. Select Build > Build APK(s). Output: `app/build/outputs/apk/debug/app-debug.apk`.

## Scope / limitations
UI-only demo: no real audio playback, Yandex account login, likes synchronization, or TS18 launcher embedding. Compact/full mode only changes the app's own layout. Debug APK is for local testing, not publication. Build is not verified in this environment.
