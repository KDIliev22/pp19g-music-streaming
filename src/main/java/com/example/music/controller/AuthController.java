package com.example.music.controller;

import com.example.music.dto.UserView;
import com.example.music.model.AppUser;
import com.example.music.repository.UserRepository;
import java.security.Principal;
import java.util.Collections;
import java.util.Optional;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final UserRepository users;

    public AuthController(UserRepository users) { this.users = users; }

    @GetMapping("/csrf")
    java.util.Map<String, String> csrf(CsrfToken t) { return java.util.Map.of("header", t.getHeaderName(), "token", t.getToken()); }

    /** The logged-in user's profile (incl. role), or {"username": null} when anonymous. */
    @GetMapping("/me")
    Object me(Principal p) {
        Optional<AppUser> u = p == null ? Optional.empty() : users.findByUsername(p.getName());
        return u.isPresent() ? UserView.of(u.get()) : Collections.<String, Object>singletonMap("username", null);
    }
}
