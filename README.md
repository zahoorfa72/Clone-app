# Clone App — Android app/profile manager

Native Kotlin + Jetpack Compose application targeting Android phones, including the Google Pixel 7a as the initial test device.

## Current implementation
- Native Android dashboard.
- Detects whether this installation is running in a managed/work profile.
- Lists apps visible to the current Android user/profile.
- Launches apps in the current profile.
- Imports APK files through Android's system Package Installer.
- Explains the real Android profile-isolation boundary.
- GitHub Actions workflow builds a debug APK.

## Important platform boundary
This project does **not** claim to create full virtual Android devices. A regular third-party APK cannot silently create arbitrary Android users/work profiles, change protected hardware identifiers, or guarantee that another app sees a new physical device. Work profiles must be provisioned through Android's supported device-policy / enterprise enrollment flow. Install this manager separately in each profile to manage the apps visible there.

The app does not spoof device identity, defeat app attestation, or bypass third-party anti-abuse controls. App isolation is provided by Android only when the operating system has actually provisioned separate profiles.

## Build
Open the project in Android Studio with JDK 17 and Android SDK 35, or run:

```bash
gradle assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

## Device test plan
1. Install on the Pixel 7a.
2. Confirm profile status says Personal Android profile.
3. Import a trusted APK and complete the Android installer flow.
4. Confirm installed apps appear and launch.
5. If a supported work profile is provisioned by Android, install this app there and confirm it reports Managed / work profile.
6. Verify app data separation by testing in each profile independently.

## Security
Only install APKs from sources you trust. Android may require the user to allow installation from the selected file provider. The manager does not download APKs from unknown sources.
