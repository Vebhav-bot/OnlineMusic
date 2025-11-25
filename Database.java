package dao;

import java.sql.*;

public class Database {

    private static final String URL = "jdbc:mysql://localhost:3306/musicapp?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "Vebhav@123";

    // DO NOT KEEP A STATIC SHARED CONNECTION
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Driver not found", e);
        }

        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        ensureSchema(conn);
        return conn;
    }

    private static void ensureSchema(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS users (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100), email VARCHAR(120) UNIQUE, password_hash VARCHAR(255), role VARCHAR(20))");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS music (id INT AUTO_INCREMENT PRIMARY KEY, title VARCHAR(200), artist_name VARCHAR(150), album VARCHAR(150), genre VARCHAR(100), filepath VARCHAR(255), approved TINYINT DEFAULT 0, upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, streams INT DEFAULT 0, likes INT DEFAULT 0)");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS playlists (id INT AUTO_INCREMENT PRIMARY KEY, listener_id INT NOT NULL, title VARCHAR(200))");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS playlist_songs (playlist_id INT NOT NULL, music_id INT NOT NULL, PRIMARY KEY (playlist_id, music_id))");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS follows (listener_id INT NOT NULL, artist_name VARCHAR(150) NOT NULL, PRIMARY KEY (listener_id, artist_name))");
            // NEW: log table used in transactional approval
            st.executeUpdate("CREATE TABLE IF NOT EXISTS approval_log (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "music_id INT NOT NULL, " +
                    "admin_id INT NOT NULL, " +
                    "approved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        }
    }

    public static void runInTransaction(TransactionBody body) throws SQLException {
        try (Connection conn = getConnection()) {
            boolean old = conn.getAutoCommit();
            conn.setAutoCommit(false);

            try {
                body.run(conn);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(old);
            }
        }
    }

    public interface TransactionBody {
        void run(Connection conn) throws SQLException;
    }
}
