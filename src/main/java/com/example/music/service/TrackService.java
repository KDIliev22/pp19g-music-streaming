package com.example.music.service;

import com.example.music.dto.TrackDto;
import com.example.music.model.Track;
import com.example.music.repository.TrackRepository;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TrackService {
    private static final Set<String> TYPES = Set.of("mp3", "wav", "ogg", "m4a", "flac");
    private final TrackRepository tracks;
    private final Path media;

    public TrackService(TrackRepository tracks, @Value("${app.media-dir}") Path media) { this.tracks = tracks; this.media = media; }

    public List<TrackDto> search(String q) {
        return tracks.findByTitleContainingIgnoreCaseOrArtistContainingIgnoreCase(q, q).stream().map(TrackDto::of).toList();
    }

    public Resource audio(Long id) {
        Track t = tracks.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Track not found"));
        Resource file = new FileSystemResource(media.resolve(t.getFilename()));
        if (!file.exists()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Audio file missing");
        return file;
    }

    public TrackDto upload(String title, String artist, MultipartFile file) throws IOException {
        if (title.isBlank() || artist.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title and artist are required");
        String ext = Optional.ofNullable(StringUtils.getFilenameExtension(file.getOriginalFilename())).orElse("").toLowerCase();
        if (!TYPES.contains(ext)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use mp3, wav, ogg, m4a or flac");
        String name = UUID.randomUUID() + "." + ext; // generated name: no path traversal
        file.transferTo(media.resolve(name).toAbsolutePath());
        return TrackDto.of(tracks.save(new Track(null, title.trim(), artist.trim(), "Single", name)));
    }
}
