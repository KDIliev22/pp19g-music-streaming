package com.example.music.service;

import com.example.music.dto.CreateUser;
import com.example.music.dto.ProfileUpdate;
import com.example.music.dto.UserView;
import com.example.music.model.AppUser;
import com.example.music.model.Role;
import com.example.music.repository.UserRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/** Business rules for users. Format validation lives on the DTOs (@Valid); here: uniqueness and self-protection. */
@Service
public class UserService {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public UserService(UserRepository users, PasswordEncoder encoder) { this.users = users; this.encoder = encoder; }

    public List<UserView> list() { return users.findAll(Sort.by("username")).stream().map(UserView::of).toList(); }

    public UserView create(CreateUser c) {
        if (users.existsByUsername(c.username())) throw new ResponseStatusException(HttpStatus.CONFLICT, "Потребителското име е заето");
        String email = blankToNull(c.email() == null ? null : c.email().toLowerCase());
        requireUniqueEmail(email, null);
        return UserView.of(users.save(new AppUser(c.username(), encoder.encode(c.password()), c.fullName().trim(),
                email, blankToNull(c.phone()), c.role())));
    }

    public UserView updateProfile(Long id, ProfileUpdate p) {
        AppUser u = get(id);
        String email = blankToNull(p.email() == null ? null : p.email().toLowerCase());
        requireUniqueEmail(email, id);
        u.setFullName(p.fullName().trim()); u.setEmail(email); u.setPhone(blankToNull(p.phone()));
        return UserView.of(users.save(u));
    }

    public UserView updateOwnProfile(String username, ProfileUpdate p) {
        return updateProfile(users.findByUsername(username).orElseThrow().getId(), p);
    }

    public UserView changeRole(Long id, Role role, String actor) {
        AppUser u = get(id);
        if (u.getUsername().equals(actor) && u.getRole() != role) throw bad("Не можете да променяте собствената си роля");
        u.setRole(role);
        return UserView.of(users.save(u));
    }

    /** Soft delete / restore; the account filter enforces it on the next request. */
    public UserView setActive(Long id, boolean active, String actor) {
        AppUser u = get(id);
        if (!active && u.getUsername().equals(actor)) throw bad("Не можете да деактивирате собствения си акаунт");
        u.setActive(active);
        return UserView.of(users.save(u));
    }

    private void requireUniqueEmail(String email, Long selfId) {
        if (email != null && users.findByEmail(email).filter(o -> !o.getId().equals(selfId)).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Този email вече се използва");
    }

    private AppUser get(Long id) {
        return users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Потребителят не е намерен"));
    }

    private static String blankToNull(String s) { return s == null || s.isBlank() ? null : s.trim(); }
    private static ResponseStatusException bad(String msg) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg); }
}
