package dao;

import models.User;
import models.Admin;
import models.Artist;
import models.Listener;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAO {

    public static int createUser(String name, String email, String passwordHash, User.Role role) throws SQLException {
        String sql = "INSERT INTO users(name,email,password_hash,role) VALUES(?,?,?,?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            ps.setString(4, role.name());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public static Optional<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT id,name,email,role FROM users WHERE email=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String role = rs.getString("role");
                    switch (User.Role.valueOf(role)) {
                        case ADMIN: return Optional.of(new Admin(id, name, email));
                        case ARTIST: return Optional.of(new Artist(id, name, email));
                        default: return Optional.of(new Listener(id, name, email));
                    }
                }
            }
        }
        return Optional.empty();
    }

    public static boolean verifyPassword(String email, String passwordHash) throws SQLException {
        String sql = "SELECT password_hash FROM users WHERE email=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String stored = rs.getString("password_hash");
                    return stored.equals(passwordHash);
                }
            }
        }
        return false;
    }

    public static List<User> getAllUsers() throws SQLException {
        List<User> out = new ArrayList<>();
        String sql = "SELECT id,name,email,role FROM users";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                String role = rs.getString("role");
                switch (User.Role.valueOf(role)) {
                    case ADMIN: out.add(new Admin(id, name, email)); break;
                    case ARTIST: out.add(new Artist(id, name, email)); break;
                    default: out.add(new Listener(id, name, email)); break;
                }
            }
        }
        return out;
    }

    public static Optional<User> findById(int id) throws SQLException {
        String sql = "SELECT id,name,email,role FROM users WHERE id=?";
        try (Connection c = Database.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("name");
                    String email = rs.getString("email");
                    User.Role role = User.Role.valueOf(rs.getString("role"));
                    switch (role) {
                        case ADMIN: return Optional.of(new Admin(id, name, email));
                        case ARTIST: return Optional.of(new Artist(id, name, email));
                        default: return Optional.of(new Listener(id, name, email));
                    }
                }
            }
        }
        return Optional.empty();
    }
}
