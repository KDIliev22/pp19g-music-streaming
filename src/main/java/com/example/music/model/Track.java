package com.example.music.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
public class Track {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String title;
    @Column(nullable = false) private String artist;
    private String album;
    @Column(nullable = false) private String filename; // never expose disk names

    protected Track() {}
    public Track(Long id, String title, String artist, String album, String filename) {
        this.id = id; this.title = title; this.artist = artist; this.album = album; this.filename = filename;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAlbum() { return album; }
    public String getFilename() { return filename; }

    @Override public boolean equals(Object o) { return o instanceof Track t && id != null && id.equals(t.id); }
    @Override public int hashCode() { return Objects.hashCode(id); }
}
