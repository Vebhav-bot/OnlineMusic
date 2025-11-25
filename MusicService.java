package service;

import dao.*;
import models.*;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MusicService {

    private final Map<Integer, User> userCache = new ConcurrentHashMap<>();
    private final Map<Integer, Music> musicCache = new ConcurrentHashMap<>();

    // lock object to demonstrate explicit synchronization
    private final Object cacheLock = new Object();

    // background analytics thread (multithreading demo)
    private final Thread analyticsThread;
    private volatile boolean analyticsRunning = true;

    public MusicService() {
        analyticsThread = new Thread(new AnalyticsWorker(), "AnalyticsWorker");
        analyticsThread.setDaemon(true);
        analyticsThread.start();
    }

    /**
     * Load users and music into in-memory caches.
     * This is useful at startup to warm caches for UI.
     */
    public void loadAll() throws SQLException {
        synchronized (cacheLock) {
            userCache.clear();
            musicCache.clear();

            // load users
            List<User> users = UserDAO.getAllUsers();
            for (User u : users) userCache.put(u.getId(), u);

            // load all music (both approved and pending)
            List<Music> mus = MusicDAO.getAllMusic(false);
            for (Music m : mus) musicCache.put(m.getId(), m);
        }
    }

    /* ---------------------- User operations ---------------------- */

    public int createUser(String name, String email, User.Role role, String password) throws SQLException {
        int id = AuthService.register(name, email, password, role);
        // optionally refresh cache entry
        if (id > 0) {
            try {
                User created = UserDAO.findById(id).orElseThrow(() -> new SQLException("Created user not found"));
                synchronized (cacheLock) {
                    userCache.put(id, created);
                }
            } catch (SQLException ex) {
                // ignore cache fill failure (DB has user anyway)
            }
        }
        return id;
    }

    /* ---------------------- Music upload / retrieval ---------------------- */

    public int uploadMusic(String title, String artistName, String album, String genre, String filepath) throws SQLException {
        int id = MusicDAO.insertMusic(title, artistName, album, genre, filepath, false);
        if (id > 0) {
            // fetch the freshly inserted row and cache it
            Music m = MusicDAO.findById(id);
            if (m != null) {
                synchronized (cacheLock) {
                    musicCache.put(id, m);
                }
            }
        }
        return id;
    }

    public List<Music> getApprovedMusic() throws SQLException {
        // return fresh list from DB (not from cache) to reflect DB truth
        return MusicDAO.getAllMusic(true);
    }

    public List<Music> getAllMusic() throws SQLException {
        return MusicDAO.getAllMusic(false);
    }

    /**
     * Approve or un-approve a music record WITH TRANSACTION + LOG.
     * Updates both the music table and approval_log atomically.
     */
    public void approveMusic(int musicId, boolean approved, int adminId) throws SQLException {
        Database.runInTransaction(conn -> {
            try (PreparedStatement ps1 = conn.prepareStatement(
                         "UPDATE music SET approved=? WHERE id=?");
                 PreparedStatement ps2 = conn.prepareStatement(
                         "INSERT INTO approval_log (music_id, admin_id) VALUES (?, ?)")) {

                // update music.approved
                ps1.setInt(1, approved ? 1 : 0);
                ps1.setInt(2, musicId);
                ps1.executeUpdate();

                // create log entry
                ps2.setInt(1, musicId);
                ps2.setInt(2, adminId);
                ps2.executeUpdate();
            }
        });

        // refresh cache after transaction
        Music fresh = MusicDAO.findById(musicId);
        if (fresh != null) {
            synchronized (cacheLock) {
                musicCache.put(musicId, fresh);
            }
        }
    }

    // backward-compatible overload (admin id unknown / not tracked)
    public void approveMusic(int musicId, boolean approved) throws SQLException {
        approveMusic(musicId, approved, 0);
    }

    /**
     * Increment stream count in DB and refresh cache value.
     * Uses synchronized block to demonstrate concurrency handling.
     */
    public void streamMusic(int musicId) throws SQLException {
        MusicDAO.incrementStream(musicId);
        Music fresh = MusicDAO.findById(musicId);
        if (fresh != null) {
            synchronized (cacheLock) {
                musicCache.put(musicId, fresh);
            }
        }
    }

    /**
     * Increment like count in DB and refresh cache value.
     */
    public void likeMusic(int musicId) throws SQLException {
        MusicDAO.incrementLike(musicId);
        Music fresh = MusicDAO.findById(musicId);
        if (fresh != null) {
            synchronized (cacheLock) {
                musicCache.put(musicId, fresh);
            }
        }
    }

    /* ---------------------- Playlist operations ---------------------- */

    public int createPlaylist(int listenerId, String title) throws SQLException {
        return PlaylistDAO.createPlaylist(listenerId, title);
    }

    public List<Playlist> getPlaylistsForListener(int listenerId) throws SQLException {
        return PlaylistDAO.getPlaylistsForListener(listenerId);
    }

    public void addSongToPlaylist(int playlistId, int musicId) throws SQLException {
        PlaylistDAO.addSongToPlaylist(playlistId, musicId);
    }

    public void removeSongFromPlaylist(int playlistId, int musicId) throws SQLException {
        PlaylistDAO.removeSongFromPlaylist(playlistId, musicId);
    }

    public void deletePlaylist(int playlistId) throws SQLException {
        PlaylistDAO.deletePlaylist(playlistId);
    }

    /* ---------------------- Follow operations ---------------------- */

    public void followArtist(int listenerId, String artistName) throws SQLException {
        FollowDAO.followArtist(listenerId, artistName);
    }

    public void unfollowArtist(int listenerId, String artistName) throws SQLException {
        FollowDAO.unfollowArtist(listenerId, artistName);
    }

    public List<String> getFollowedArtists(int listenerId) throws SQLException {
        return FollowDAO.getFollowedArtists(listenerId);
    }

    /* ---------------------- Stats for charts ---------------------- */

    /**
     * Returns total streams per artist (approved music only).
     */
    public Map<String, Integer> getStreamsByArtist() throws SQLException {
        Map<String, Integer> map = new HashMap<>();
        List<Music> approved = MusicDAO.getAllMusic(true);
        for (Music m : approved) {
            map.put(m.getArtistName(), map.getOrDefault(m.getArtistName(), 0) + m.getStreams());
        }
        return map;
    }

    /* ---------------------- Multithreading Worker ---------------------- */

    private class AnalyticsWorker implements Runnable {
        @Override
        public void run() {
            while (analyticsRunning) {
                try {
                    Map<String, Integer> stats = getStreamsByArtist();
                    // simple console output – enough to prove background processing
                    System.out.println("AnalyticsWorker stats (streams by artist): " + stats);
                    Thread.sleep(10_000); // every 10 seconds
                } catch (SQLException e) {
                    System.err.println("AnalyticsWorker DB error: " + e.getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    /** Stop background thread (called when UI is closing). */
    public void shutdown() {
        analyticsRunning = false;
        analyticsThread.interrupt();
    }
}
