package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FollowDAO {
    public static void followArtist(int listenerId, String artistName) throws SQLException {
        String sql = "INSERT IGNORE INTO follows(listener_id,artist_name) VALUES(?,?)";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, listenerId); ps.setString(2, artistName);
            ps.executeUpdate();
        }
    }

    public static void unfollowArtist(int listenerId, String artistName) throws SQLException {
        String sql = "DELETE FROM follows WHERE listener_id=? AND artist_name=?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, listenerId); ps.setString(2, artistName);
            ps.executeUpdate();
        }
    }

    public static List<String> getFollowedArtists(int listenerId) throws SQLException {
        List<String> out = new ArrayList<>();
        String sql = "SELECT artist_name FROM follows WHERE listener_id=?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, listenerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(rs.getString("artist_name"));
            }
        }
        return out;
    }
}
