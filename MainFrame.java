package ui;

import dao.MusicDAO;
import models.Music;
import models.Playlist;
import models.User;
import service.MusicService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainFrame extends JFrame {

    private final MusicService service;
    private final User loggedUser;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private JTable table;
    private DefaultTableModel model;

    // Player
    private JLabel lblTitle, lblArtist;
    private JSlider seek;
    private JButton btnPlayPause;

    private Integer nowPlayingId = null;
    private boolean isPlaying = false;

    private JTextField searchField;

    public MainFrame(MusicService service, User user) {
        super("Music Player — " + user.getDisplayInfo());
        this.service = service;
        this.loggedUser = user;

        setSize(1100, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                executor.shutdownNow();
                service.shutdown();  // NEW — stops analytics thread
            }
        });

        initUI();
        loadMusic();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        add(buildSidebar(), BorderLayout.WEST);
        add(buildSearchBar(), BorderLayout.NORTH);
        add(buildCenterTable(), BorderLayout.CENTER);
        add(buildPlayerBar(), BorderLayout.SOUTH);
    }

    // ---------------------- SIDEBAR ----------------------

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setPreferredSize(new Dimension(200, getHeight()));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(new EmptyBorder(10, 10, 10, 10));

        side.add(makeButton("Home", e -> loadMusic()));
        side.add(makeButton("Search", e -> searchField.requestFocus()));
        side.add(makeButton("Playlists", e -> openPlaylists()));
        side.add(makeButton("Library", e -> loadMusic()));

        if (loggedUser.getRole() == User.Role.ARTIST)
            side.add(makeButton("Upload", e -> showUploadDialog()));

        if (loggedUser.getRole() == User.Role.ADMIN)
            side.add(makeButton("Approvals", e -> openApprovals()));

        side.add(Box.createVerticalGlue());
        side.add(makeButton("Settings", e -> showSettings()));
        side.add(makeButton("Logout", e -> logout()));

        return side;
    }

    private JButton makeButton(String text, ActionListener act) {
        JButton b = new JButton(text);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.addActionListener(act);
        return b;
    }

    // ------------------ SEARCH BAR ----------------------

    private JPanel buildSearchBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        searchField = new JTextField();
        JButton searchBtn = new JButton("Search");

        searchBtn.addActionListener(e -> searchMusic());
        searchField.addActionListener(e -> searchMusic());

        p.add(searchField, BorderLayout.CENTER);
        p.add(searchBtn, BorderLayout.EAST);

        return p;
    }

    // ------------------ TABLE CENTER ----------------------

    private JScrollPane buildCenterTable() {

        model = new DefaultTableModel(
                new Object[]{"ID", "Title", "Artist", "Album", "Approved"}, 0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2)
                    playSelected();
            }
        });

        return new JScrollPane(table);
    }

    // ---------------- PLAYER BAR -----------------------

    private JPanel buildPlayerBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBorder(new EmptyBorder(10, 10, 10, 10));

        lblTitle = new JLabel("No Track Playing");
        lblArtist = new JLabel("");

        JPanel left = new JPanel(new GridLayout(2, 1));
        left.add(lblTitle);
        left.add(lblArtist);

        btnPlayPause = new JButton("Play");
        JButton next = new JButton("Next");
        JButton prev = new JButton("Prev");

        seek = new JSlider(0, 100, 0);
        seek.setEnabled(false);

        btnPlayPause.addActionListener(e -> togglePlayPause());
        next.addActionListener(e -> nextTrack());
        prev.addActionListener(e -> prevTrack());

        JPanel controls = new JPanel();
        controls.add(prev);
        controls.add(btnPlayPause);
        controls.add(next);

        bar.add(left, BorderLayout.WEST);
        bar.add(seek, BorderLayout.CENTER);
        bar.add(controls, BorderLayout.EAST);

        return bar;
    }

    // ------------------ ACTION HANDLERS ----------------------

    private void loadMusic() {
        try {
            model.setRowCount(0);

            List<Music> list = (loggedUser.getRole() == User.Role.ADMIN)
                    ? service.getAllMusic()
                    : service.getApprovedMusic();

            for (Music m : list) {
                model.addRow(new Object[]{
                        m.getId(), m.getTitle(), m.getArtistName(),
                        m.getAlbum(), m.isApproved() ? "Yes" : "No"
                });
            }

        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void searchMusic() {
        String q = searchField.getText().trim().toLowerCase();
        if (q.isEmpty()) {
            loadMusic();
            return;
        }

        try {
            model.setRowCount(0);

            List<Music> list = (loggedUser.getRole() == User.Role.ADMIN)
                    ? service.getAllMusic()
                    : service.getApprovedMusic();

            for (Music m : list) {

                String combined = (m.getTitle() + " " + m.getArtistName() + " " +
                        (m.getAlbum() == null ? "" : m.getAlbum())).toLowerCase();

                if (combined.contains(q)) {
                    model.addRow(new Object[]{
                            m.getId(), m.getTitle(), m.getArtistName(),
                            m.getAlbum(), m.isApproved() ? "Yes" : "No"
                    });
                }
            }

        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void playSelected() {
        int r = table.getSelectedRow();
        if (r < 0) {
            showError("Select a track");
            return;
        }

        int id = (int) model.getValueAt(r, 0);
        nowPlayingId = id;

        try {
            Music m = MusicDAO.findById(id);
            lblTitle.setText(m.getTitle());
            lblArtist.setText(m.getArtistName());
        } catch (SQLException ignored) {}

        startPlayback(id);
    }

    private void startPlayback(int id) {
        isPlaying = true;
        btnPlayPause.setText("Pause");
        seek.setEnabled(true);
        seek.setValue(0);

        executor.submit(() -> {
            try { service.streamMusic(id); } catch (Exception ignored) {}

            for (int i = 0; i <= 100; i++) {
                if (!isPlaying) break;

                int v = i;
                SwingUtilities.invokeLater(() -> seek.setValue(v));

                try { Thread.sleep(150); } catch (Exception ignored) {}
            }

            SwingUtilities.invokeLater(() -> {
                btnPlayPause.setText("Play");
                isPlaying = false;
            });
        });
    }

    private void togglePlayPause() {
        if (nowPlayingId == null) {
            showError("No song selected.");
            return;
        }

        if (isPlaying) {
            isPlaying = false;
            btnPlayPause.setText("Play");
        } else {
            startPlayback(nowPlayingId);
        }
    }

    private void nextTrack() {
        int r = table.getSelectedRow();
        if (r < table.getRowCount() - 1) {
            table.setRowSelectionInterval(r + 1, r + 1);
            playSelected();
        }
    }

    private void prevTrack() {
        int r = table.getSelectedRow();
        if (r > 0) {
            table.setRowSelectionInterval(r - 1, r - 1);
            playSelected();
        }
    }

    private void openPlaylists() {
        try {
            new PlaylistManagerDialog(this, service, loggedUser.getId()).setVisible(true);
        } catch (SQLException ex) {
            showError(ex);
        }
    }

    private void openApprovals() {
        try {
            List<Music> all = service.getAllMusic();
            StringBuilder sb = new StringBuilder();

            for (Music m : all) {
                if (!m.isApproved())
                    sb.append(String.format("[%d] %s - %s%n", m.getId(), m.getTitle(), m.getArtistName()));
            }

            String pending = sb.toString();

            if (pending.isBlank()) {
                JOptionPane.showMessageDialog(this, "No pending approvals");
                return;
            }

            String input = JOptionPane.showInputDialog(this,
                    pending + "\n\nEnter ID to approve:");

            if (input == null || input.isBlank()) return;

            int id = Integer.parseInt(input.trim());

            service.approveMusic(id, true, loggedUser.getId());   // NEW — transactional approval
            JOptionPane.showMessageDialog(this, "Approved successfully!");

            loadMusic();

        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void showUploadDialog() {
        JPanel p = new JPanel(new GridLayout(0, 1));
        JTextField title = new JTextField();
        JTextField album = new JTextField();
        JTextField genre = new JTextField();
        JTextField path = new JTextField();

        p.add(new JLabel("Title:")); p.add(title);
        p.add(new JLabel("Album:")); p.add(album);
        p.add(new JLabel("Genre:")); p.add(genre);
        p.add(new JLabel("File Path:")); p.add(path);

        int ok = JOptionPane.showConfirmDialog(this, p, "Upload Music", JOptionPane.OK_CANCEL_OPTION);

        if (ok == JOptionPane.OK_OPTION) {
            try {
                int id = service.uploadMusic(
                        title.getText(),
                        loggedUser.getName(),
                        album.getText(),
                        genre.getText(),
                        path.getText()
                );

                loadMusic();
                JOptionPane.showMessageDialog(this, "Uploaded (pending approval). ID: " + id);

            } catch (Exception ex) {
                showError(ex);
            }
        }
    }

    private void showSettings() {
        JOptionPane.showMessageDialog(this, "Settings not available yet.");
    }

    private void logout() {
        dispose();
        new LoginFrame(service).setVisible(true);
    }

    private void showError(Object msg) {
        JOptionPane.showMessageDialog(this, msg.toString(), "Error", JOptionPane.ERROR_MESSAGE);
    }
}
