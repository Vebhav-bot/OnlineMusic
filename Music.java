package models;

public class Music {
    private int id;
    private String title;
    private String artistName;
    private String album;
    private String genre;
    private String filepath;
    private boolean approved;
    private int streams;
    private int likes;

    public Music(int id, String title, String artistName, String album, String genre, String filepath, boolean approved, int streams, int likes) {
        this.id = id; this.title = title; this.artistName = artistName; this.album = album;
        this.genre = genre; this.filepath = filepath; this.approved = approved; this.streams = streams; this.likes = likes;
    }

    public int getId(){ return id; }
    public String getTitle(){ return title; }
    public String getArtistName(){ return artistName; }
    public String getAlbum(){ return album; }
    public String getGenre(){ return genre; }
    public String getFilepath(){ return filepath; }
    public boolean isApproved(){ return approved; }
    public void setApproved(boolean b){ approved = b; }
    public int getStreams(){ return streams; }
    public int getLikes(){ return likes; }

    @Override
    public String toString(){
        return String.format("[%d] %s - %s (%s) %s", id, title, artistName, album != null ? album : "Single", approved ? "✔" : "✖");
    }
}
