package com.example.music.repository;

import com.example.music.model.Track;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackRepository extends JpaRepository<Track, Long> {
    List<Track> findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(String title, String artist);
}
