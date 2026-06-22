# Blog App

A modern Android application for reading and sharing blog posts. Users can create, edit, and save articles, as well as manage their profiles.

## Features
- **User Authentication**: Sign in and registration using Firebase Auth.
- **Blog Feed**: Browse through various blog posts.
- **CRUD Operations**: Add, Edit, and Delete your own blog articles.
- **Save Articles**: Bookmark articles for later reading.
- **Image Support**: Upload and display images using Firebase Storage and Glide.
- **User Profiles**: Manage personal information and see your own posts.

## Tech Stack
- **Language**: Kotlin
- **Architecture**: MVVM (implied) / Traditional Android Development
- **Database**: Firebase Realtime Database
- **Authentication**: Firebase Authentication
- **Storage**: Firebase Storage
- **UI Components**: Material Design, ConstraintLayout, ViewBinding
- **Image Loading**: Glide

## Folder Structure
```text
Blog-App/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/shreyaspawar/blogapp/
│   │   │   │   ├── Model/                # Data models for blogs and users
│   │   │   │   ├── adapter/              # RecyclerView adapters
│   │   │   │   ├── register/             # Auth related logic
│   │   │   │   ├── MainActivity.kt       # Main landing page
│   │   │   │   ├── SplashActivity.kt     # Splash screen
│   │   │   │   ├── ArticleActivity.kt    # Individual article view
│   │   │   │   └── ...                   # Other activities
│   │   │   ├── res/
│   │   │   │   ├── layout/               # XML layouts
│   │   │   │   ├── drawable/             # Image resources
│   │   │   │   └── values/               # Strings, colors, etc.
│   │   │   └── AndroidManifest.xml
│   ├── build.gradle.kts                  # App-level build configuration
│   └── google-services.json              # Firebase configuration
├── build.gradle.kts                      # Project-level build configuration
├── settings.gradle.kts                   # Project settings
└── README.md                             # This file
```

## Getting Started
1. Clone the repository.
2. Add your `google-services.json` in the `app/` folder.
3. Build the project using Android Studio.
4. Run the app on an emulator or physical device.

<!---## Screenshots--->
<!--- *(Add screenshots here if available)*
--->