# QGS -> IP-NET Hosting rebrand

Separate work branch for the requested QGS Console APK rebrand.

## Required result
- Preserve the original QGS screens, navigation, and behavior.
- App name: **IP-NET Hosting**
- Panel URL: **https://ptero.ip-net.ro**
- Theme: background #090A0D, panel #151820, accent #F32643, muted text #9AA0AC.
- Use the supplied IP-NET Hosting logo for the in-app and launcher branding.

## Important source note
The supplied input is a compiled APK, not an original Android source project. Decompiled code is not the original source tree. Decode the APK with Apktool and inspect its DEX with JADX before changing resources, manifest, or networking. Rebuild and sign only after the original app behavior is preserved and tested.

The original APK itself has not been committed in this branch.


## Implementation boundary

The supplied QGS package is retained only as a behavior and layout reference. The distributable IP-NET app is implemented independently in `app/` with Kotlin and Jetpack Compose; the QGS package/license-check code is not part of the native app build. APK packaging remains disabled until the native app is complete and validated.
