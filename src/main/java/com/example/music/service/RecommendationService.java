package com.example.music.service;

import com.example.music.dto.RecommendationDto;
import com.example.music.dto.TrackDto;
import com.example.music.model.Playlist;
import com.example.music.model.Track;
import com.example.music.repository.PlaylistRepository;
import com.example.music.repository.TrackRepository;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Explainable recommender (no ML library needed), combining three signals:
 *  1. collaborative: tracks that sit in playlists overlapping with the user's own tracks (weighted by overlap share),
 *  2. content-based: tracks by an artist the user already has,
 *  3. popularity: number of playlists containing the track (tie-breaker, and the only signal for new users).
 */
@Service
public class RecommendationService {
    private final PlaylistRepository playlists;
    private final TrackRepository tracks;

    public RecommendationService(PlaylistRepository playlists, TrackRepository tracks) { this.playlists = playlists; this.tracks = tracks; }

    public List<RecommendationDto> forUser(String username, int limit) {
        Map<Long, Track> byId = tracks.findAll().stream().collect(Collectors.toMap(Track::getId, Function.identity()));
        Set<Long> seeds = new HashSet<>(); // what the user already has
        playlists.findByOwnerUsername(username).forEach(p -> p.getTracks().forEach(t -> seeds.add(t.getId())));

        Map<Long, double[]> score = new HashMap<>(); // [collaborative, content, popularity]
        for (Playlist p : playlists.findAll()) {
            Set<Long> ids = p.getTracks().stream().map(Track::getId).collect(Collectors.toSet());
            long overlap = ids.stream().filter(seeds::contains).count();
            for (Long id : ids) {
                if (seeds.contains(id)) continue;
                double[] s = score.computeIfAbsent(id, k -> new double[3]);
                s[2] += 1;
                if (overlap > 0) s[0] += (double) overlap / ids.size();
            }
        }
        Set<String> artists = seeds.stream().map(byId::get).map(Track::getArtist).collect(Collectors.toSet());
        byId.values().stream().filter(t -> !seeds.contains(t.getId()) && artists.contains(t.getArtist()))
                .forEach(t -> score.computeIfAbsent(t.getId(), k -> new double[3])[1] = 1);

        return score.entrySet().stream().map(e -> {
            double[] s = e.getValue();
            String reason = s[0] > 0 ? "Often added together with songs you like"
                    : s[1] > 0 ? "Same artist as songs you like" : "Popular in playlists";
            double total = 2 * s[0] + s[1] + 0.1 * s[2];
            return new RecommendationDto(TrackDto.of(byId.get(e.getKey())), Math.round(total * 100) / 100.0, reason);
        }).sorted(Comparator.comparingDouble(RecommendationDto::score).reversed()
                .thenComparing(r -> r.track().title())).limit(limit).toList();
    }
}
