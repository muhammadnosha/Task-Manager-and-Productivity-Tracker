# Task Manager & Productivity Tracker

A lightweight Android app to help users organize daily tasks, set reminders, and track productivity over time. The app provides an intuitive interface for creating, categorizing, and managing tasks plus a productivity tracker that reports completed tasks, time spent, and progress toward goals. The project is intended to be practical and deployable (publishable to Google Play) and gives hands-on experience with Android development, local storage, and a small Python backend for syncing and backups.

## Main Features
- Create, edit and delete tasks
- Priority categories (High, Medium, Low)
- Deadlines and reminder notifications
- Productivity tracking: completed tasks and time spent
- Visual summaries of productivity (charts/graphs)
- Offline-first with local storage and background sync when online
- Daily productivity summary notifications
- Export task lists / reports (PDF or CSV)
- Dark mode support
- Cloud backup and data sync across devices (via Python backend)

## Tech Stack
- Kotlin — Android app (UI, business logic)
- SQLite — Local, offline-first storage on device
- Python — Lightweight backend API for sync, backup, and multi-device data

## Architecture
- Android client (Kotlin) handles UI, local persistence (SQLite), reminders/notifications, and offline usage.
- Python backend exposes RESTful endpoints used for user authentication, cloud backup, and syncing across devices.
- App synchronizes local changes to the backend when network is available.

## Quick Start

1. Clone the repository
   git clone https://github.com/muhammadnosha/Task-Manager-and-Productivity-Tracker.git
2. Android app
   - Open the Android project in Android Studio
   - Let Gradle sync and resolve dependencies
   - Run on an emulator or physical device (API 21+ recommended)
3. Backend (optional, for sync/backup)
   - Navigate to the backend directory (if provided)
   - Create a virtual environment and install dependencies:
     python -m venv venv
     source venv/bin/activate  # Windows: venv\Scripts\activate
     pip install -r requirements.txt
   - Configure environment variables (e.g., DB connection, SECRET keys)
   - Start the server:
     python app.py
4. Configure app backend URL
   - In the Android app configuration (constants or build config), set the backend base URL to your running server.
