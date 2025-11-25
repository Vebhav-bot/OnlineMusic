package models;

import java.util.ArrayList;
import java.util.List;

public class Playlist {
    private int id;
    private String title;
    private List<Music> songs = new ArrayList<>();

    public Playlist(int id, String title){
        this.id = id; this.title = title;
    }
    public Playlist(String title){ this(0, title); }

    public int getId(){ return id; }
    public String getTitle(){ return title; }
    public List<Music> getSongs(){ return songs; }

    public void addSong(Music m){ if (!songs.contains(m)) songs.add(m); }
    public void removeSong(Music m){ songs.remove(m); }

    @Override
    public String toString(){ return title + " (" + songs.size() + " songs)"; }
}
