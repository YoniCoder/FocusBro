# FocusBro - Pomodoro Timer App

A fully-featured Pomodoro timer for Android with background operation, analytics, and persistent session tracking.

## Features

- **Customizable Tags** – Create, edit, delete focus tags (Work, Study, Reading, etc.)
- **Adjustable Timer** – Set duration per tag (1-180 minutes)
- **Circular Timer Animation** – Visual countdown with pie-style progress
- **Background Timer** – Continue counting when app is closed (Foreground Service)
- **Persistent Notification** – Tap notification to return to active session
- **Analytics Dashboard** – Track focus time by period (Today, Week, Month, Year)
- **Pie Chart** – Visualize time breakdown by tag
- **Session History** – All completed sessions saved to database
- **Custom UI** – Apple-style minimalist design

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose
- **Database:** Room (SQLite)
- **Services:** Foreground Service, WakeLock
- **Architecture:** MVVM pattern
- **Notifications:** Android Notifications API

## Screenshots

### Home Screen
![Home Screen](screenshots/screenshot1.png)

### Active Session
![Active Session](screenshots/screenshot2.png)

### Analytics Dashboard
![Analytics](screenshots/screenshot3.png)

### Tags Management
![Tags](screenshots/screenshot4.png)

## What I Learned

Building FocusBro taught me:
- Foreground Services for background timer operation
- WakeLock management (CPU stays awake without draining battery)
- Room Database for complex time-based queries
- Data visualization (Pie charts in Android)
- System theme integration (light/dark mode)
- Android permissions handling (Notifications on Android 13+)
- UX best practices (preventing accidental quits, confirmation dialogs)

## How to Run

1. Clone: `git clone https://github.com/YoniCoder/FocusBro.git`
2. Open in **Android Studio**
3. Build → Run (Android 8+)

## Future Improvements

- Cloud sync across devices
- Team focus sessions
- Calendar integration
- Smart break suggestions
- Theme selection

## Contact

📧 yonastedla06@gmail.com  
📱 +251707106234
🔗 [GitHub](https://github.com/YoniCoder)
