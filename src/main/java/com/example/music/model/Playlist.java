package com.example.music.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Playlist {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String name;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) private AppUser owner;
    @ManyToMany(fetch = FetchType.EAGER) private Set<Track> tracks = new HashSet<>();

    protected Playlist() {}
    public Playlist(Long id, String name, AppUser owner, Set<Track> tracks) {
        this.id = id; this.name = name; this.owner = owner; this.tracks = tracks;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public AppUser getOwner() { return owner; }
    public Set<Track> getTracks() { return tracks; }
}
