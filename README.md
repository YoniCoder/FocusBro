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
<img width="720" height="1600" alt="screenshot 1" src="https://github.com/user-attachments/assets/5ac045c6-3578-42a0-83d6-dfe2c2ebbd5c" />


### Active Session
<img width="720" height="1600" alt="screenshot 2" src="https://github.com/user-attachments/assets/65868083-359c-4873-bfb5-2086e4e928b1" />


### Analytics Dashboard
<img width="720" height="1600" alt="screenshot 3" src="https://github.com/user-attachments/assets/2e65d659-92f4-4156-aa83-ae500d7b0e92" />


### Tags Management
<img width="720" height="1600" alt="screenshot 4" src="https://github.com/user-attachments/assets/d8027629-84ae-44cb-94b7-f5e8538bed99" />


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
