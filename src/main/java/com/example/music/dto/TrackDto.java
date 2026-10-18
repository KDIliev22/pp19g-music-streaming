package com.example.music.dto;

import com.example.music.model.Track;
public record TrackDto(Long id, String title, String artist, String album) {
    public static TrackDto of(Track t) { return new TrackDto(t.getId(), t.getTitle(), t.getArtist(), t.getAlbum()); }
}
