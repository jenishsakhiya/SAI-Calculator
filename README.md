# SAI Soft Jaws Calculator (Android)

Offline Android app that wraps the SAI Soft Jaws price calculator
(`app/src/main/assets/index.html`) in a full-screen WebView.
No internet permission; the standard discount is remembered on the phone.

## Option A: build on GitHub (no Android Studio needed)
1. Create a new **private** GitHub repository and push this folder to it
   (or drag-and-drop all files on github.com → "Add file → Upload files";
   keep the `.github` folder).
2. Open the repo's **Actions** tab → "Build APK" runs automatically on every push
   (or click "Run workflow").
3. When it finishes (~3–5 min), open the run → **Artifacts** →
   download `SAI-Soft-Jaws-Calculator` (a zip containing the .apk).

## Option B: Android Studio
Open this folder → wait for Gradle sync → **Build → Build App Bundle(s)/APK(s) → Build APK(s)**.
Output: `app/build/outputs/apk/debug/app-debug.apk`.

Command line (Android SDK + JDK 17 installed): `./gradlew assembleDebug`

## Install on the phone
Copy the .apk to the phone and open it. Android will ask to allow
"Install unknown apps" for your file manager / browser the first time.

## Updating prices or adding products
Edit `app/src/main/assets/index.html` (the `PRODUCTS` list), bump
`versionCode` in `app/build.gradle`, and rebuild. Installing the new APK
over the old one keeps the saved standard discount, as long as it is built
on the same machine/workflow (same debug signing key).

## Notes
- This builds a *debug-signed* APK, which is fine for installing on your own
  and your team's phones. For the Play Store you'd need a release keystore.
- Min Android version: 8.0 (API 26).
