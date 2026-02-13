# PrepZen - Aptitude Quiz App (Android, Kotlin)

PrepZen is an offline-first aptitude learning and quiz app built with Kotlin, MVVM, and Material 3.

## What Changed
This version removes dependency on `topics.json` and now loads data dynamically from asset folders:

```text
assets/
  LOGICAL/*.json
  QUANTITATIVE/*.json
  VERBAL/*.json
  quizzes.json (legacy/shared quiz bank supported)
```

App flow is now:

`Category -> Topic -> Quiz`

## Tech Stack
- Kotlin
- MVVM (ViewModel + Repository)
- Android Navigation Component
- Material 3 (XML UI)
- SharedPreferences for local user state (bookmarks, scores)
- AdMob test IDs (banner/interstitial)

## Architecture
- `app/src/main/java/com/prepzen/app/data`
  - `ContentRepository.kt`: dynamic asset scanning + JSON parsing
  - `UserPrefsRepository.kt`: bookmarks, viewed topics, quiz scores
- `app/src/main/java/com/prepzen/app/domain`
  - data models (`Topic`, `QuizQuestion`, `QuizCategory`, etc.)
- `app/src/main/java/com/prepzen/app/ui`
  - feature fragments, viewmodels, adapters

## Data Loading Notes
- Categories are discovered by scanning asset folders.
- Topic cards are generated from each topic JSON file.
- Quiz questions are loaded from:
  - `assets/quizzes.json` (legacy root file)
  - `assets/<CATEGORY>/quizzes.json` (if present)
  - topic file keys like `questions`, `quiz_questions`, or `quizzes`
- Empty/malformed JSON files are handled gracefully (no crash).

## Build Requirements
- Android Studio Hedgehog+ (or newer)
- JDK 17 (recommended; minimum JDK 11 for AGP 8.x)
- Android SDK configured via Android Studio SDK Manager

## Run Locally
1. Clone repo.
2. Open in Android Studio.
3. Ensure Gradle JDK is set to **17**.
4. Sync project.
5. Run app on emulator/device (minSdk 24).

## Useful Commands
```bash
./gradlew :app:assembleDebug
```
Windows:
```powershell
.\gradlew :app:assembleDebug
```

## AdMob Test IDs Included
- App ID: `ca-app-pub-3940256099942544~3347511713`
- Banner: `ca-app-pub-3940256099942544/6300978111`
- Interstitial: `ca-app-pub-3940256099942544/1033173712`

Replace with production IDs before Play Store release.
