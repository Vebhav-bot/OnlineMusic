package models;

import java.util.ArrayList;
import java.util.List;

public class Listener extends User implements Streamable {

    private List<Playlist> playlists = new ArrayList<>();

    public Listener(int id, String name, String email) {
        super(id, name, email, Role.LISTENER);
    }

    public List<Playlist> getPlaylists() { return playlists; }

    public void addPlaylist(Playlist p) { playlists.add(p); }

    @Override
    public String getDisplayInfo() {
        return "Listener: " + name + " (" + email + ")";
    }

    @Override
    public void stream(Music music) throws StreamingException {
        if (music == null) {
            throw new StreamingException("Music cannot be null.");
        }
        if (!music.isApproved()) {
            throw new StreamingException("This track is not approved for streaming yet.");
        }
        // Simple demonstration of streaming behavior for OOP rubric
        System.out.println("Listener " + name + " is streaming: " + music.getTitle()
                + " by " + music.getArtistName());
    }
}
