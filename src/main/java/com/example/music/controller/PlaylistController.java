package com.example.music.controller;

import com.example.music.dto.NewPlaylist;
import com.example.music.dto.PlaylistDto;
import com.example.music.service.PlaylistService;
import jakarta.validation.Valid;

import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {
    private final PlaylistService service;

    public PlaylistController(PlaylistService service) { this.service = service; }

    @GetMapping
    List<PlaylistDto> mine(Principal p) { return service.mine(p.getName()); }

    @PostMapping
    PlaylistDto create(@Valid @RequestBody NewPlaylist body, Principal p) { return service.create(p.getName(), body.name()); }

    @PostMapping("/{id}/tracks/{trackId}")
    PlaylistDto add(@PathVariable Long id, @PathVariable Long trackId, Principal p) { return service.addTrack(p.getName(), id, trackId); }

    @DeleteMapping("/{id}/tracks/{trackId}")
    PlaylistDto remove(@PathVariable Long id, @PathVariable Long trackId, Principal p) { return service.removeTrack(p.getName(), id, trackId); }
}
