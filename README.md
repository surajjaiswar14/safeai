# 🛡️ SafeJourney AI - Intelligent Travel & Personal Safety Companion

[![Android](https://img.shields.io/badge/Platform-Android-green.svg?style=flat&logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin_1.9+-purple.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack_Compose-4285F4.svg?style=flat&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28.svg?style=flat&logo=firebase)](https://firebase.google.com)
[![Room](https://img.shields.io/badge/Storage-Room_DB-4285F4.svg?style=flat&logo=sqlite)](https://developer.android.com/training/data-storage/room)
[![Build](https://img.shields.io/badge/Build-Passing-brightgreen.svg?style=flat)](https://github.com/surajjaiswar14/safeai)

**SafeJourney AI** is a state-of-the-art Android safety application built with Kotlin and Jetpack Compose. It provides real-time emergency SOS assistance, AI-driven safety recommendations, GPS-backed nearby safe place discovery, seamless Firebase cloud synchronization, and offline-first Room database resilience.

---

## 🌟 Key Features & Capabilities

### 1. 🚨 One-Tap Emergency SOS System
- **Instant Activation**: Emergency SOS button triggers immediate alert mode.
- **Scrollable Emergency Screen**: Complete SOS screen displays live location coordinates, current address, safety tips, and active emergency countdown without UI clipping.
- **Universal Helplines**: Pre-configured emergency numbers for instant access:
  - 👮 **Police**: `100` / `112`
  - 🚑 **Ambulance**: `102`
  - 🚒 **Fire**: `101`
  - 👩 **Women Helpline**: `1091`
  - 🧳 **Tourist Helpline**: `1363`
- **Trusted Emergency Contacts**: Add, manage, and call personal emergency contacts directly during critical situations.

### 2. 🤖 AI Safety Assistant
- **Travel Risk Analysis**: Chat with an AI assistant trained on travel safety protocols, risk assessment, and emergency guidance.
- **Context-Aware Assistance**: Get immediate advice for unfamiliar locations, solo night travel, or unexpected hazards.

### 3. 📍 Real GPS & Safe Nearby Places
- **Real-Time GPS Location**: Access current latitude, longitude, and street-level location via Google Play Services Fused Location.
- **Safe Place Finder**: Instant search for nearby safe zones:
  - 🏬 Police Stations
  - 🏥 Hospitals & Emergency Rooms
  - ⛽ Safe Fuel Stations & 24/7 Rest Stops
  - 🛡️ Safe Havens & Shelters
- **Distance & Rating Details**: Live distance calculations and safety ratings for nearby destinations.

### 4. 🔐 Firebase Auth & Cloud Firestore Sync
- **Real Authentication**: Complete sign-in, account registration, and password recovery via Firebase Auth.
- **Automatic Cloud Backup**: `DataSyncManager` automatically synchronizes local user data, emergency contacts, profile photo, and settings with Cloud Firestore in real time.

### 5. 👥 User Profile & Customization
- **Profile Persistence**: Custom avatar image selection, full user details, and emergency health info persistence.
- **Theme Preferences**: Seamless toggle between Dark Mode, Light Mode, and System Default theme (`ThemeScreen`).
- **Multi-Language Support**: Built-in language picker supporting localized UI experiences (`LanguageScreen`).

### 6. 🗺️ Saved Destinations & Safety Advisories
- **Bookmarked Destinations**: Save home, work, and frequent destinations directly from the Home screen.
- **Real-Time Safety Advisories**: Live advisories and weather alert updates for safer route planning.

### 7. 💾 Offline-First Resilience & Local Database Tools
- **Room Database Seeding**: Pre-loaded SQLite database (`DatabaseSeeder.kt`) ensures full functionality even without internet connectivity.
- **Instant DB Inspection Tool**: Included PowerShell inspection script (`show_db.ps1`) to inspect local Room DB tables in real-time from the terminal.

---

## 📱 App Navigation & Screen Directory

| Screen | Description |
|---|---|
| **Splash / Onboarding** | Welcome experience & app permission onboarding flow |
| **Login & Register** | Real Firebase Authentication screens with loading & validation states |
| **Home Screen** | Quick access to SOS, search bar, active location, saved destinations, and profile summary |
| **SOS Activation Screen** | Scrollable emergency dashboard with live coordinates, countdown, and helpline triggers |
| **AI Assistant Screen** | Conversational travel safety guidance and risk advisory chat |
| **Nearby Places Screen** | Categorized view of safe locations with real distance calculations |
| **Emergency Contacts** | List of universal helplines and custom trusted personal contacts |
| **Profile Screen** | User profile manager, emergency medical info, and avatar customizer |
| **Settings / Theme / Language** | System preferences, dark/light theme toggle, and language selector |

---

## 🛠️ Technology Stack & Architecture

- **Language**: Kotlin 1.9+
- **UI Framework**: Jetpack Compose with Material 3 & Navigation Compose
- **Architecture**: MVVM (Model-View-ViewModel) with Clean Architecture principles
- **Asynchronous Flow**: Kotlin Coroutines & `StateFlow` / `SharedFlow`
- **Local Database**: Room DB (SQLite) with KSP annotation processor
- **Cloud Infrastructure**: Firebase Authentication, Cloud Firestore, Firebase Storage
- **Location Services**: Google Play Services `FusedLocationProviderClient`
- **Image Loading**: Coil for Compose

---

## 🚀 Quick Setup & How to Run for Collaborators

Follow these steps to run **SafeJourney AI** on your local machine or Android device:

### Prerequisites
- **Android Studio**: Ladybug, Jellyfish, Koala, or higher
- **JDK**: Java 17
- **Android SDK**: API Level 34 / 35 (Minimum SDK: 24 / Android 7.0)

### Step-by-Step Guide

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/surajjaiswar14/safeai.git
   ```

2. **Open in Android Studio**:
   - Launch Android Studio and click **Open**.
   - Select the `SafeJourneyAI` folder.

3. **Firebase & Google Services**:
   - The repository includes the pre-configured `app/google-services.json` file out of the box.

4. **Build the Project**:
   - Run Gradle sync or execute in terminal:
     ```bash
     ./gradlew assembleDebug
     ```

5. **Run the App**:
   - Connect your Android physical device or start an Android Emulator.
   - Click **Run (Shift + F10)** in Android Studio.

6. **Inspect Local Room Database (Optional)**:
   - Run the included PowerShell inspector script:
     ```powershell
     powershell -ExecutionPolicy Bypass -File .\show_db.ps1
     ```

---

## 📄 License & Attribution

Developed as part of the **SafeJourney AI** initiative for intelligent travel and emergency safety.

---
*Built with ❤️ for traveler safety and peace of mind.*
