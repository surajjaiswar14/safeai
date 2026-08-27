# SafeJourneyAI — Final Backend, Database & Firestore Integration Architecture

## 1. Architecture Overview
SafeJourneyAI utilizes an **Offline-First MVVM + Repository Architecture** with local Room SQLite persistence and asynchronous background Cloud Firestore synchronization.

```
                  ┌──────────────────────────────┐
                  │  UI (Jetpack Compose Screens) │
                  └──────────────┬───────────────┘
                                 │
                                 ▼
                  ┌──────────────────────────────┐
                  │   ViewModels (StateFlow)     │
                  └──────────────┬───────────────┘
                                 │
                                 ▼
                  ┌──────────────────────────────┐
                  │ Repository Layer (Single     │
                  │      Source of Truth)        │
                  └──────┬────────────────┬──────┘
                         │                │
     (Immediate Local)   │                │   (Async Background)
                         ▼                ▼
           ┌──────────────────┐      ┌─────────────────────────┐
           │ Room SQLite DB   │      │ Cloud Firestore         │
           │ (safejourney.db) │      │ (Firebase Cloud Sync)   │
           └──────────────────┘      └─────────────────────────┘
```

- **Room SQLite Database**: Local primary storage (`safejourney.db` v2) serving UI states instantly via reactive `Flow`/`StateFlow`. Works 100% offline without network dependencies.
- **Firebase Authentication**: User authentication via Email/Password & Guest Mode persistence.
- **Cloud Firestore Background Sync**: Background coroutines push user data changes to Firestore under user-isolated paths when online.

---

## 2. Firestore Document Structure & Security Rules

### Document & Subcollection Paths:
- **`users/{uid}`**: User Profile document (`uid`, `displayName`, `email`, `phone`, `photoUrl`, `createdAt`, `updatedAt`).
- **`users/{uid}/saved_destinations/{destinationId}`**: User-saved destinations (`destinationId`, `savedAt`).
- **`users/{uid}/emergency_contacts/{contactId}`**: Custom user emergency contacts (`id`, `name`, `type`, `phoneNumber`, `description`, `isDefault`).
- **`users/{uid}/offline_packs/{destinationId}`**: Offline safety packs metadata (`destinationId`, `name`, `state`, `downloadedAt`).
- **`users/{uid}/checklists/{itemId}`**: User checklist item states (`id`, `isCompleted`, `updatedAt`).
- **`destinations/{destinationId}`**: Public destination catalog (Read-only).
- **`advisories/{advisoryId}`**: Public advisories catalog (Read-only).

### Security Rules (`firestore.rules`):
```rules
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
    
    // User profile document and all user subcollections
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;

      match /{allSubcollections=**} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
    
    // Fallback collections for top-level user queries
    match /saved_destinations/{docId} {
      allow read, write: if request.auth != null && request.auth.uid == resource.data.userId;
      allow create: if request.auth != null && request.auth.uid == request.resource.data.userId;
    }
    
    match /emergency_contacts/{docId} {
      allow read, write: if request.auth != null && request.auth.uid == resource.data.userId;
      allow create: if request.auth != null && request.auth.uid == request.resource.data.userId;
    }

    match /offline_packs/{docId} {
      allow read, write: if request.auth != null && request.auth.uid == resource.data.userId;
      allow create: if request.auth != null && request.auth.uid == request.resource.data.userId;
    }

    match /checklists/{docId} {
      allow read, write: if request.auth != null && request.auth.uid == resource.data.userId;
      allow create: if request.auth != null && request.auth.uid == request.resource.data.userId;
    }
    
    // Public destination catalog
    match /destinations/{destinationId} {
      allow read: if true;
      allow write: if false;
    }

    // Public advisories
    match /advisories/{advisoryId} {
      allow read: if true;
      allow write: if false;
    }
  }
}
```

---

## 3. Files Modified / Created
- `app/google-services.json`: Official Firebase configuration for package `com.safejourneyai.app`.
- `app/build.gradle.kts`: Applied Google Services plugin and updated `applicationId = "com.safejourneyai.app"`.
- `firestore.rules`: Updated with recursive subcollection permissions for `users/{userId}`.
- `app/src/main/java/com/example/safejourneyai/data/repository/SafeJourneyRepository.kt`: Added background Firestore sync helpers for saved destinations, emergency contacts, offline packs, and checklists.
- `app/src/main/java/com/example/safejourneyai/data/repository/AuthRepository.kt`: Firebase Auth flow integration with user profile Firestore sync.

---

## 4. Verification Results
- `./gradlew clean`: **BUILD SUCCESSFUL** (Exit code 0)
- `./gradlew assembleDebug`: **BUILD SUCCESSFUL** (Exit code 0)
- `./gradlew test`: **BUILD SUCCESSFUL** (Exit code 0)
- **Debug APK Output**: `app/build/outputs/apk/debug/app-debug.apk`

---

## 5. Manual Setup Steps in Firebase Console
1. **Deploy Firestore Rules**: Run `firebase deploy --only firestore:rules` or copy the contents of `firestore.rules` directly into the Rules tab of your Firebase Console.
2. **Enable Authentication Methods**: Under Firebase Console > Authentication > Sign-in method, ensure **Email/Password** is enabled.
