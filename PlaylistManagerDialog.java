package ui;

import models.Playlist;
import models.Music;
import service.MusicService;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class PlaylistManagerDialog extends JDialog {
    private MusicService service;
    private int listenerId;
    private DefaultListModel<String> playlistsModel = new DefaultListModel<>();
    private JList<String> playlistsList = new JList<>(playlistsModel);

    public PlaylistManagerDialog(Frame owner, MusicService service, int listenerId) throws SQLException {
        super(owner, "Manage Playlists", true);
        this.service = service; this.listenerId = listenerId;
        setSize(600,400);
        setLocationRelativeTo(owner);
        initUI();
        loadPlaylists();
    }

    private void initUI() {
        setLayout(new BorderLayout(8,8));
        add(new JScrollPane(playlistsList), BorderLayout.CENTER);

        JPanel btns = new JPanel();
        JButton newBtn = new JButton("New");
        JButton deleteBtn = new JButton("Delete");
        JButton viewBtn = new JButton("View/Edit");
        btns.add(newBtn); btns.add(deleteBtn); btns.add(viewBtn);
        add(btns, BorderLayout.SOUTH);

        newBtn.addActionListener(e -> {
            String title = JOptionPane.showInputDialog(this, "Playlist title:");
            if (title == null || title.trim().isEmpty()) return;
            try {
                service.createPlaylist(listenerId, title);
                loadPlaylists();
            } catch (SQLException ex) { JOptionPane.showMessageDialog(this, ex.getMessage()); }
        });

        deleteBtn.addActionListener(e -> {
            String sel = playlistsList.getSelectedValue();
            if (sel == null) return;
            int pid = Integer.parseInt(sel.split(" - ")[0]);
            try {
                service.deletePlaylist(pid);
                loadPlaylists();
            } catch (SQLException ex) { JOptionPane.showMessageDialog(this, ex.getMessage()); }
        });

        viewBtn.addActionListener(e -> {
            String sel = playlistsList.getSelectedValue();
            if (sel == null) return;
            int pid = Integer.parseInt(sel.split(" - ")[0]);
            try {
                Playlist p = service.getPlaylistsForListener(listenerId).stream().filter(pl -> pl.getId() == pid).findFirst().orElse(null);
                if (p != null) {
                    new PlaylistEditorDialog(this, service, p).setVisible(true);
                    loadPlaylists();
                }
            } catch (SQLException ex) { JOptionPane.showMessageDialog(this, ex.getMessage()); }
        });
    }

    private void loadPlaylists() throws SQLException {
        playlistsModel.clear();
        List<Playlist> pls = service.getPlaylistsForListener(listenerId);
        for (Playlist p : pls) {
            playlistsModel.addElement(p.getId() + " - " + p.getTitle() + " (" + p.getSongs().size() + " songs)");
        }
    }
}
