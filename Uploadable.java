package models;

public interface Uploadable {
    void upload(Music music) throws UploadException;
}
