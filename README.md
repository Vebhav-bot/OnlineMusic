This is a full Music Streaming Desktop Application built in Java Swing, inspired by Spotify but simplified for academic use.

It fulfills all Review-1 Rubric Requirements — OOP, Collections, JDBC, Multithreading, Transactions, and model-based structure.

The platform supports multiple user roles:

Admin

Artist

Listener

✨ Features
👤 User Roles
Role	Description
Admin	Approves music uploads, views all songs
Artist	Uploads songs, views own uploads
Listener	Plays music, creates playlists, follows artists
🎧 Core Music Features

Music Upload (Artists)

Admin Approval System

Search by Title / Album / Artist

Local file-based streaming simulation

Play/Pause/Next/Previous Controls

Volume Control

Artwork display

📁 Playlist System

Create/Delete Playlists

Add music to playlists

View playlist songs

❤️ Follow System

Listeners follow artists

Follows are stored in separate normalized table

🧩 Tech Stack
Layer	Technology
Frontend	Java Swing
Backend	Java OOP + Services
Database	MySQL + JDBC
Architecture	MVC + DAO Pattern
Threading	ExecutorService + SwingWorker
Extras	Custom Exceptions, Transaction Handling
🏗️ Project Architecture
src/
 ├── ui/
 │    ├── LoginFrame.java
 │    ├── MainFrame.java
 │    └── PlaylistManagerDialog.java
 │
 ├── models/
 │    ├── User.java (Admin/Artist/Listener using inheritance)
 │    ├── Music.java
 │    ├── Playlist.java
 │    ├── exceptions/
 │         ├── InvalidEmailException.java
 │         └── MusicNotFoundException.java
 │
 ├── dao/
 │    ├── DBConnection.java
 │    ├── UserDAO.java
 │    ├── MusicDAO.java
 │    └── PlaylistDAO.java
 │
 ├── service/
 │    ├── AuthService.java
 │    ├── MusicService.java
 │    └── PlaylistService.java
 │
 └── Main.java
