package com.example.music.controller;

import com.example.music.dto.CreateUser;
import com.example.music.dto.ProfileUpdate;
import com.example.music.dto.RoleChange;
import com.example.music.dto.UserView;
import com.example.music.service.UserService;
import jakarta.validation.Valid;

import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** /api/users/** is ADMIN-only (see SecurityConfig). /api/profile is for any logged-in user. */
@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService service;

    public UserController(UserService service) { this.service = service; }

    @GetMapping("/users")
    List<UserView> list() { return service.list(); }

    @PostMapping("/users") @ResponseStatus(HttpStatus.CREATED)
    UserView create(@Valid @RequestBody CreateUser body) { return service.create(body); }

    @PutMapping("/users/{id}")
    UserView update(@PathVariable Long id, @Valid @RequestBody ProfileUpdate body) { return service.updateProfile(id, body); }

    @PutMapping("/users/{id}/role")
    UserView role(@PathVariable Long id, @Valid @RequestBody RoleChange body, Principal p) { return service.changeRole(id, body.role(), p.getName()); }

    @DeleteMapping("/users/{id}")
    UserView deactivate(@PathVariable Long id, Principal p) { return service.setActive(id, false, p.getName()); }

    @PostMapping("/users/{id}/activate")
    UserView activate(@PathVariable Long id, Principal p) { return service.setActive(id, true, p.getName()); }

    @PutMapping("/profile")
    UserView profile(@Valid @RequestBody ProfileUpdate body, Principal p) { return service.updateOwnProfile(p.getName(), body); }
}
