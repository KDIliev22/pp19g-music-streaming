package com.example.music.controller;

import com.example.music.dto.RecommendationDto;
import com.example.music.service.RecommendationService;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
    private final RecommendationService service;

    public RecommendationController(RecommendationService service) { this.service = service; }

    @GetMapping
    List<RecommendationDto> forMe(Principal p, @RequestParam(defaultValue = "6") int limit) {
        return service.forUser(p.getName(), Math.min(Math.max(limit, 1), 20));
    }
}
