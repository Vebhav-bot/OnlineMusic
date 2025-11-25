package ui;

import models.User;
import service.AuthService;
import service.MusicService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.Optional;

public class LoginFrame extends JFrame {

    private MusicService musicService;

    // Colors (matching MainFrame)
    private static final Color BG = new Color(18, 18, 18);
    private static final Color PANEL = new Color(28, 28, 28);
    private static final Color RED = new Color(229, 45, 39);
    private static final Color TEXT = Color.WHITE;
    private static final Color MUTED = new Color(180, 180, 180);

    public LoginFrame(MusicService musicService) {
        this.musicService = musicService;

        setTitle("Music App - Login");
        setSize(480, 420);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        initUI();
    }

    private void initUI() {

        getContentPane().setBackground(BG);
        setLayout(new GridBagLayout());

        JPanel card = new JPanel();
        card.setBackground(PANEL);
        card.setPreferredSize(new Dimension(360, 330));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(60,60,60)),
                new EmptyBorder(20, 24, 20, 24)
        ));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("MusicApp Login");
        title.setForeground(TEXT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 18)));

        // Text fields
        JTextField emailF = new JTextField();
        styleTextField(emailF, "Email");

        JPasswordField passF = new JPasswordField();
        styleTextField(passF, "Password");

        // Password toggle eye
        JButton eyeBtn = new JButton("👁");
        eyeBtn.setFocusable(false);
        eyeBtn.setBackground(PANEL);
        eyeBtn.setForeground(TEXT);
        eyeBtn.setBorder(null);
        eyeBtn.setPreferredSize(new Dimension(40, 36));

        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordPanel.setBackground(PANEL);
        passwordPanel.add(passF, BorderLayout.CENTER);
        passwordPanel.add(eyeBtn, BorderLayout.EAST);

        eyeBtn.addActionListener(e -> {
            if (passF.getEchoChar() == 0) {
                passF.setEchoChar('•');
            } else {
                passF.setEchoChar((char) 0);
            }
        });

        JButton loginBtn = new JButton("Login");
        loginBtn.setBackground(RED);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        loginBtn.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Register label link
        JLabel reg = new JLabel("<HTML><U>Create a new account</U></HTML>");
        reg.setForeground(RED);
        reg.setCursor(new Cursor(Cursor.HAND_CURSOR));
        reg.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(makeLabel("Email"));
        card.add(emailF);
        card.add(Box.createRigidArea(new Dimension(0, 12)));

        card.add(makeLabel("Password"));
        card.add(passwordPanel);
        card.add(Box.createRigidArea(new Dimension(0, 18)));

        card.add(loginBtn);
        card.add(Box.createRigidArea(new Dimension(0, 12)));

        card.add(reg);

        add(card);

        /* ---------------- LOGIN FUNCTION ---------------- */

        loginBtn.addActionListener(e -> {
            String email = emailF.getText().trim();
            String pass = new String(passF.getPassword());

            if (email.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Email and Password required!");
                return;
            }

            try {
                Optional<User> user = AuthService.login(email, pass);

                if (user.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Invalid credentials!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                MainFrame mf = new MainFrame(musicService, user.get());
                mf.setVisible(true);
                dispose();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        /* ---------------- REGISTER FUNCTION ---------------- */

        reg.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

                String email = emailF.getText().trim();
                String pass = new String(passF.getPassword());

                if (email.isEmpty() || pass.isEmpty()) {
                    JOptionPane.showMessageDialog(LoginFrame.this,
                            "Email & Password required before registering!");
                    return;
                }

                // Ask for Name using styled dialog
                String name = showStyledNameDialog();
                if (name == null) return;

                // Ask for Role using custom attractive dialog
                User.Role role = showStyledRoleDialog();
                if (role == null) {
                    JOptionPane.showMessageDialog(LoginFrame.this, "Role selection required!");
                    return;
                }

                try {
                    musicService.createUser(name, email, role, pass);
                    JOptionPane.showMessageDialog(LoginFrame.this, "Registration Successful!");
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(LoginFrame.this, ex.getMessage(),
                            "Registration Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    /* ---------------- CUSTOM DIALOGS ---------------- */

    private String showStyledNameDialog() {
        JTextField nameField = new JTextField();
        nameField.setBackground(new Color(40,40,40));
        nameField.setForeground(Color.WHITE);
        nameField.setCaretColor(Color.WHITE);
        nameField.setBorder(new EmptyBorder(8,10,8,10));

        Object[] msg = {
                new JLabel("Enter your Name:"),
                nameField
        };

        int opt = JOptionPane.showConfirmDialog(
                this, msg, "User Name",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (opt == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name is required!");
                return null;
            }
            return name;
        }
        return null;
    }

    private User.Role showStyledRoleDialog() {

        JPanel rolePanel = new JPanel();
        rolePanel.setLayout(new GridLayout(3, 1, 8, 8));
        rolePanel.setBackground(new Color(30, 30, 30));
        rolePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton adminBtn = styledRoleButton("ADMIN");
        JButton artistBtn = styledRoleButton("ARTIST");
        JButton listenerBtn = styledRoleButton("LISTENER");

        rolePanel.add(adminBtn);
        rolePanel.add(artistBtn);
        rolePanel.add(listenerBtn);

        final User.Role[] selected = {null};

        adminBtn.addActionListener(e -> {
            selected[0] = User.Role.ADMIN;
            SwingUtilities.getWindowAncestor(rolePanel).dispose();
        });
        artistBtn.addActionListener(e -> {
            selected[0] = User.Role.ARTIST;
            SwingUtilities.getWindowAncestor(rolePanel).dispose();
        });
        listenerBtn.addActionListener(e -> {
            selected[0] = User.Role.LISTENER;
            SwingUtilities.getWindowAncestor(rolePanel).dispose();
        });

        JDialog dialog = new JDialog(this, "Choose User Role", true);
        dialog.setContentPane(rolePanel);
        dialog.setSize(300, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        return selected[0];
    }

    private JButton styledRoleButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(40, 40, 40));
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(new Color(60,60,60)); }
            public void mouseExited(MouseEvent e) { b.setBackground(new Color(40,40,40)); }
        });
        return b;
    }

    /* ---------------- UI HELPERS ---------------- */

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(MUTED);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return lbl;
    }

    private void styleTextField(JTextField tf, String placeholder) {
        tf.setBackground(new Color(40, 40, 40));
        tf.setForeground(TEXT);
        tf.setCaretColor(Color.WHITE);
        tf.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        tf.setPreferredSize(new Dimension(300, 36));
        tf.putClientProperty("JTextField.placeholderText", placeholder);
    }
}
