# Lite Notes

Simple, offline, ad-free notes and checklists. Pure Android Java — no libraries, no ads, no trackers, **no internet permission**.

## Features
- Notes and checklists, colors, pinning, search
- Home-screen widget
- Share text into a new note
- Export all notes to Markdown, backup & restore to a file you choose
- English, Azerbaijani and Russian (follows phone language)

## Permissions
- None

## Build
Open the folder in Android Studio (AGP 9.2.1, Gradle 9.4.1, compileSdk 35) and press **Run**.
Release builds are minified with R8. To sign a release, put a `keystore.properties` file
(`storeFile`, `storePassword`, `keyAlias`, `keyPassword`) in the project root — it is git-ignored.

## Privacy
Lite Notes collects no data. See [PRIVACY.md](PRIVACY.md) or the
[online privacy policy](https://aliaghayev132.github.io/lite-notes/).

---
**AZ:** Lite Notes — reklamsız, internetsiz, yüngül Android tətbiqi. Heç bir məlumat toplamır.
**RU:** Lite Notes — лёгкое приложение для Android без рекламы и без доступа к интернету. Не собирает данные.
