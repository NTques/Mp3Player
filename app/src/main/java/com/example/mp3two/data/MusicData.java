package com.example.mp3two.data;

import java.io.Serializable;

public class MusicData implements Serializable {
    private long id;
    private long albumId;
    private String title;
    private String artist;
    private String album;
    private long duration;
    private String dataPath;

    public MusicData() {
    }

    public MusicData(long id, long albumId, String title, String artist, String album, long duration, String dataPath) {
        this.id = id;
        this.albumId = albumId;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.dataPath = dataPath;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }
}
