package service;

import dao.UserDAO;
import models.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Optional;

public class AuthService {

    public static String hashPassword(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(plain.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte x : b) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static Optional<User> login(String email, String plainPassword) throws SQLException {
        String hash = hashPassword(plainPassword);
        if (UserDAO.verifyPassword(email, hash)) {
            return UserDAO.findByEmail(email);
        }
        return Optional.empty();
    }

    public static int register(String name, String email, String password, User.Role role) throws SQLException {
        String hash = hashPassword(password);
        return UserDAO.createUser(name, email, hash, role);
    }
}
