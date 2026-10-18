package com.example.music.controller;

import com.example.music.dto.RegistrationReport;
import com.example.music.service.RegistrationService;
import org.springframework.web.bind.annotation.*;

/** ADMIN-only: /api/admin/** is restricted in SecurityConfig. */
@RestController
@RequestMapping("/api/admin")
public class RegistrationController {
    private final RegistrationService service;

    public RegistrationController(RegistrationService service) { this.service = service; }

    /** GET /api/admin/registrations?period=TODAY|WEEK|MONTH|YEAR */
    @GetMapping("/registrations")
    RegistrationReport registrations(@RequestParam(defaultValue = "TODAY") RegistrationService.Period period) {
        return service.report(period);
    }
}
