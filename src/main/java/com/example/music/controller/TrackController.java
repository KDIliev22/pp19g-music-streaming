package com.example.music.controller;

import com.example.music.dto.TrackDto;
import com.example.music.service.TrackService;
import java.io.IOException;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/tracks")
public class TrackController {
    private final TrackService service;

    public TrackController(TrackService service) { this.service = service; }

    @GetMapping
    List<TrackDto> search(@RequestParam(defaultValue = "") String q) { return service.search(q); }

    /** Spring MVC answers HTTP Range requests for Resource bodies (206), so seeking works. */
    @GetMapping("/{id}/stream")
    ResponseEntity<Resource> stream(@PathVariable Long id) {
        Resource audio = service.audio(id);
        return ResponseEntity.ok().contentType(MediaTypeFactory.getMediaType(audio).orElse(MediaType.APPLICATION_OCTET_STREAM)).body(audio);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    TrackDto upload(@RequestParam String title, @RequestParam String artist, @RequestParam MultipartFile file) throws IOException {
        return service.upload(title, artist, file);
    }
}
