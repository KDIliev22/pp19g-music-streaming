package com.example.music.dto;

import com.example.music.model.Playlist;
import java.util.Comparator;
import java.util.List;

public record PlaylistDto(Long id, String name, List<TrackDto> tracks) {
    public static PlaylistDto of(Playlist p) {
        return new PlaylistDto(p.getId(), p.getName(),
                p.getTracks().stream().map(TrackDto::of).sorted(Comparator.comparing(TrackDto::title)).toList());
    }
}
