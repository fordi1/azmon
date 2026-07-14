# Azmon - Exam PDF Designer

Azmon is an offline Android application for creating structured exam papers and exporting them as print-ready A4 PDF files. The interface and generated documents are designed for Persian content, including right-to-left text and embedded Persian fonts.

## Features

- Create and manage exam papers locally
- Add multiple-choice, descriptive, fill-in-the-blank, and true/false questions
- Configure question scores, answer space, options, and images
- Enter school, university, course, teacher, student, and exam details
- Organize questions and optionally group them by type
- Choose from multiple document templates
- Preview, save, and share generated PDF files
- Work without an internet connection or API key

## Tech stack

- Kotlin
- Jetpack Compose and Material 3
- Room database
- Kotlin Coroutines and Flow
- Android PDF APIs
- Coil for image loading
- JUnit, Robolectric, and Roborazzi for testing

## Requirements

- Android Studio with JDK 17
- Android SDK 36
- An Android device or emulator running Android 6.0 (API 23) or later

## Getting started

1. Clone the repository:

   ```bash
   git clone <repository-url>
   cd azmon
   ```

2. Open the project in Android Studio.
3. Allow Gradle to synchronize and download the required dependencies.
4. Select an emulator or connected Android device and run the `app` configuration.

You can also build a debug APK from the command line:

```bash
./gradlew assembleDebug
```

On Windows, use:

```powershell
.\gradlew.bat assembleDebug
```

The APK will be generated under `app/build/outputs/apk/debug/`.

## Testing

Run the local unit and rendering tests with:

```bash
./gradlew test
```

On Windows:

```powershell
.\gradlew.bat test
```

## Project structure

```text
app/src/main/java/com/example/
├── data/       Room entities, DAO, database, and repository
├── model/      Navigation and domain models
├── pdf/        PDF generation
├── ui/         Compose screens, components, and theme
└── utils/      Backup and text utilities
```

## Privacy

Exam data is stored locally on the device. The core application does not require an internet connection, a cloud account, or an API key.

## License

No license has been specified yet. Add a `LICENSE` file before distributing or accepting contributions if you want to define reuse terms.
