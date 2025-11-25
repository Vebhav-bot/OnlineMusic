package dao;

import models.Music;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MusicDAO {

    public static int insertMusic(String title, String artistName, String album, String genre, String filepath, boolean approved) throws SQLException {
        String sql = "INSERT INTO music(title,artist_name,album,genre,filepath,approved) VALUES(?,?,?,?,?,?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title);
            ps.setString(2, artistName);
            ps.setString(3, album);
            ps.setString(4, genre);
            ps.setString(5, filepath);
            ps.setInt(6, approved ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public static List<Music> getAllMusic(boolean onlyApproved) throws SQLException {
        List<Music> out = new ArrayList<>();
        String sql = "SELECT id,title,artist_name,album,genre,filepath,approved,streams,likes FROM music";
        if (onlyApproved) sql += " WHERE approved=1";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
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
        return out;
    }

    public static Music findById(int id) throws SQLException {
        String sql = "SELECT id,title,artist_name,album,genre,filepath,approved,streams,likes FROM music WHERE id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Music(id, rs.getString("title"),
                            rs.getString("artist_name"),
                            rs.getString("album"),
                            rs.getString("genre"),
                            rs.getString("filepath"),
                            rs.getInt("approved") == 1,
                            rs.getInt("streams"),
                            rs.getInt("likes"));
                }
            }
        }
        return null;
    }

    public static void setApproved(int musicId, boolean approved) throws SQLException {
        String sql = "UPDATE music SET approved=? WHERE id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, approved ? 1 : 0);
            ps.setInt(2, musicId);
            ps.executeUpdate();
        }
    }

    public static void incrementStream(int musicId) throws SQLException {
        String sql = "UPDATE music SET streams = streams + 1 WHERE id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, musicId);
            ps.executeUpdate();
        }
    }

    public static void incrementLike(int musicId) throws SQLException {
        String sql = "UPDATE music SET likes = likes + 1 WHERE id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, musicId);
            ps.executeUpdate();
        }
    }
}
