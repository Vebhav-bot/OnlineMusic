package ui;

import models.Playlist;
import models.Music;
import service.MusicService;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class PlaylistEditorDialog extends JDialog {
    private MusicService service;
    private Playlist playlist;
    private DefaultListModel<String> songsModel = new DefaultListModel<>();
    private JList<String> songsList = new JList<>(songsModel);
    private DefaultListModel<String> allMusicModel = new DefaultListModel<>();
    private JList<String> allMusicList = new JList<>(allMusicModel);

    public PlaylistEditorDialog(Dialog owner, MusicService service, Playlist playlist) throws SQLException {
        super(owner, "Edit Playlist - " + playlist.getTitle(), true);
        this.service = service; this.playlist = playlist;
        setSize(800,500);
        setLocationRelativeTo(owner);
        initUI();
        loadContent();
    }

    private void initUI() {
        setLayout(new GridLayout(1,2,8,8));
        JPanel left = new JPanel(new BorderLayout());
        left.add(new JLabel("Playlist Songs"), BorderLayout.NORTH);
        left.add(new JScrollPane(songsList), BorderLayout.CENTER);
        JButton removeBtn = new JButton("Remove Selected");
        left.add(removeBtn, BorderLayout.SOUTH);

        JPanel right = new JPanel(new BorderLayout());
        right.add(new JLabel("All Approved Music"), BorderLayout.NORTH);
        right.add(new JScrollPane(allMusicList), BorderLayout.CENTER);
        JButton addBtn = new JButton("Add Selected");
        right.add(addBtn, BorderLayout.SOUTH);

        add(left); add(right);

        removeBtn.addActionListener(e -> {
            String sel = songsList.getSelectedValue();
            if (sel == null) return;
            int musicId = Integer.parseInt(sel.substring(1, sel.indexOf(']')));
            try {
                service.removeSongFromPlaylist(playlist.getId(), musicId);
                reload();
            } catch (SQLException ex) { JOptionPane.showMessageDialog(this, ex.getMessage()); }
        });

        addBtn.addActionListener(e -> {
            String sel = allMusicList.getSelectedValue();
            if (sel == null) return;
            int musicId = Integer.parseInt(sel.substring(1, sel.indexOf(']')));
            try {
                service.addSongToPlaylist(playlist.getId(), musicId);
                reload();
            } catch (SQLException ex) { JOptionPane.showMessageDialog(this, ex.getMessage()); }
        });
    }

    private void loadContent() throws SQLException {
        reload();
        allMusicModel.clear();
        List<Music> all = service.getApprovedMusic();
        for (Music m : all) allMusicModel.addElement(m.toString());
    }

    private void reload() throws SQLException {
        songsModel.clear();
        Playlist p = service.getPlaylistsForListener(playlist.getSongs().size() == 0 ? playlist.getId() : playlist.getId()).stream()
                .filter(pl -> pl.getId() == playlist.getId()).findFirst().orElse(playlist);
        // re-fetch
        List<Playlist> pls = service.getPlaylistsForListener(p.getId()); // not ideal but works
        Playlist refreshed = null;
        for (Playlist pl: service.getPlaylistsForListener(playlist.getSongs().size() == 0 ? playlist.getId() : playlist.getId())) {
            if (pl.getId() == playlist.getId()) { refreshed = pl; break; }
        }
        if (refreshed == null) refreshed = playlist;
        for (Music m : refreshed.getSongs()) songsModel.addElement(m.toString());
    }
}
