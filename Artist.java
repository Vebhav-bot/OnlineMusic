package models;

public class Artist extends User implements Uploadable {

    public Artist(int id, String name, String email) {
        super(id, name, email, Role.ARTIST);
    }

    @Override
    public String getDisplayInfo() {
        return "Artist: " + name + " (" + email + ")";
    }

    @Override
    public void upload(Music music) throws UploadException {
        if (music == null) {
            throw new UploadException("Music cannot be null while uploading.");
        }
        if (music.getTitle() == null || music.getTitle().isBlank()) {
            throw new UploadException("Music title is required for upload.");
        }
        if (!name.equals(music.getArtistName())) {
            throw new UploadException("Artist name mismatch for this upload.");
        }
        // Just a simple demonstration of behavior
        System.out.println("Artist " + name + " is preparing upload for: " + music.getTitle());
    }
}
