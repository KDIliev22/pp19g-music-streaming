package com.example.music.controller;

import com.example.music.dto.Result;
import com.example.music.service.SearchService;
import org.springframework.web.bind.annotation.*;

/** ADMIN-only: /api/admin/** is restricted in SecurityConfig. */
@RestController
@RequestMapping("/api/admin")
public class SearchController {
    private final SearchService service;

    public SearchController(SearchService service) { this.service = service; }

    /** GET /api/admin/search?q=иван&scope=ALL|USERS|MEMBERS&sort=name&dir=asc|desc&page=0&size=10 */
    @GetMapping("/search")
    Result search(@RequestParam(defaultValue = "") String q,
                                @RequestParam(defaultValue = "ALL") SearchService.Scope scope,
                                @RequestParam(defaultValue = "name") String sort,
                                @RequestParam(defaultValue = "asc") String dir,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size) {
        return service.search(q, scope, sort, dir, page, size);
    }
}
