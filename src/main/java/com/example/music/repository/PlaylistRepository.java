package com.example.music.repository;

import com.example.music.model.Playlist;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findByOwnerUsername(String username);
    Optional<Playlist> findByIdAndOwnerUsername(Long id, String username);
}
