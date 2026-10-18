package com.example.music.service;

import com.example.music.dto.PlaylistDto;
import com.example.music.model.AppUser;
import com.example.music.model.Playlist;
import com.example.music.repository.PlaylistRepository;
import com.example.music.repository.TrackRepository;
import com.example.music.repository.UserRepository;
import java.util.HashSet;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PlaylistService {
    private final PlaylistRepository playlists;
    private final TrackRepository tracks;
    private final UserRepository users;

    public PlaylistService(PlaylistRepository playlists, TrackRepository tracks, UserRepository users) {
        this.playlists = playlists; this.tracks = tracks; this.users = users;
    }

    public List<PlaylistDto> mine(String username) {
        return playlists.findByOwnerUsername(username).stream().map(PlaylistDto::of).toList();
    }

    public PlaylistDto create(String username, String name) {
        AppUser owner = users.findByUsername(username).orElseThrow();
        return PlaylistDto.of(playlists.save(new Playlist(null, name.trim(), owner, new HashSet<>())));
    }

    public PlaylistDto addTrack(String username, Long id, Long trackId) {
        Playlist pl = owned(id, username);
        pl.getTracks().add(tracks.findById(trackId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Track not found")));
        return PlaylistDto.of(playlists.save(pl));
    }

    public PlaylistDto removeTrack(String username, Long id, Long trackId) {
        Playlist pl = owned(id, username);
        pl.getTracks().removeIf(t -> t.getId().equals(trackId));
        return PlaylistDto.of(playlists.save(pl));
    }

    private Playlist owned(Long id, String username) { // ownership check on every mutation
        return playlists.findByIdAndOwnerUsername(id, username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Playlist not found"));
    }
}
