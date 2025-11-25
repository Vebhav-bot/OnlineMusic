package dao;

import models.Playlist;
import models.Music;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PlaylistDAO {

    public static int createPlaylist(int listenerId, String title) throws SQLException {
        String sql = "INSERT INTO playlists(listener_id,title) VALUES(?,?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, listenerId);
            ps.setString(2, title);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public static List<Playlist> getPlaylistsForListener(int listenerId) throws SQLException {
        List<Playlist> out = new ArrayList<>();
        String sql = "SELECT id,title FROM playlists WHERE listener_id=?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, listenerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String title = rs.getString("title");
                    Playlist p = new Playlist(id, title);
                    // load songs
                    p.getSongs().addAll(getSongsForPlaylist(id));
                    out.add(p);
                }
            }
        }
        return out;
    }

    public static void addSongToPlaylist(int playlistId, int musicId) throws SQLException {
        String sql = "INSERT IGNORE INTO playlist_songs(playlist_id, music_id) VALUES(?,?)";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, playlistId); ps.setInt(2, musicId);
            ps.executeUpdate();
        }
    }

    public static void removeSongFromPlaylist(int playlistId, int musicId) throws SQLException {
        String sql = "DELETE FROM playlist_songs WHERE playlist_id=? AND music_id=?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, playlistId); ps.setInt(2, musicId);
            ps.executeUpdate();
        }
    }

    public static void deletePlaylist(int playlistId) throws SQLException {
        Database.runInTransaction(conn -> {
            try (PreparedStatement ps1 = conn.prepareStatement("DELETE FROM playlist_songs WHERE playlist_id=?")) {
                ps1.setInt(1, playlistId);
                ps1.executeUpdate();
            }
            try (PreparedStatement ps2 = conn.prepareStatement("DELETE FROM playlists WHERE id=?")) {
                ps2.setInt(1, playlistId);
                ps2.executeUpdate();
            }
        });
    }

    public static List<Music> getSongsForPlaylist(int playlistId) throws SQLException {
        List<Music> out = new ArrayList<>();
        String sql = "SELECT m.id,m.title,m.artist_name,m.album,m.genre,m.filepath,m.approved,m.streams,m.likes FROM music m JOIN playlist_songs ps ON m.id=ps.music_id WHERE ps.playlist_id=?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, playlistId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new Music(rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("artist_name"),
                            rs.getString("album"),
                            rs.getString("genre"),
                            rs.getString("filepath"),
                            rs.getInt("approved") == 1,
                            rs.getInt("streams"),
                            rs.getInt("likes")));
                }
            }
        }
        return out;
    }
}
