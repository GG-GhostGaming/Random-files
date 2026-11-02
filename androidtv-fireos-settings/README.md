# Fire TV / Android TV Settings Utility

This folder contains a separate Android TV application project intended for Fire OS 11 / Android 11 TV devices. It is isolated from the rest of the repository and does not modify the existing project files.

## What this app does

- Launches the main Android TV settings screens
- Opens display, sound, network, accessibility, developer and input-related settings
- Provides a basic remap / controls utility section for commonly used Fire TV / Android TV control settings
- Checks for root availability and can attempt root shell commands when the APK is installed on a rooted Fire TV

## Build instructions

1. Install Android Studio or the Android command-line SDK.
2. Ensure Java 17 is installed.
3. Set ANDROID_HOME or ANDROID_SDK_ROOT to your SDK path.
4. From this folder run:

```bash
chmod +x gradlew
./gradlew assembleDebug
```

The APK output will be:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Install on Fire TV Stick

Use ADB from a development machine:

```bash
adb connect <firetv_ip>
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

If the Fire TV is rooted and you want to force install with root:

```bash
adb shell "su -c 'pm install -r /sdcard/Download/app-debug.apk'"
```

## Notes

- This app is designed as a local, rooted support launcher for system settings and common Android TV actions.
- Android TV does not expose a generic public API for full custom key remapping for every remote input. The app therefore opens the correct settings screens when available and provides a root fallback path for system-level actions.
- Fire OS and Amazon-specific settings screens may vary by model and software build.
