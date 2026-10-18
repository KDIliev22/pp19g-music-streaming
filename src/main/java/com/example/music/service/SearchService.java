package com.example.music.service;

import com.example.music.dto.Hit;
import com.example.music.dto.Result;
import com.example.music.repository.MemberRepository;
import com.example.music.repository.UserRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Searches users and/or members as one list. Both sets are filtered in Java (not SQL) so that
 * case-insensitive matching of Cyrillic behaves the same on H2 and PostgreSQL; fine for thousands
 * of rows. For much larger tables, move filtering/paging into a database query.
 */
@Service
public class SearchService {

    public enum Scope { ALL, USERS, MEMBERS }


    /** Whitelist of sortable columns. */
    private static final Map<String, Function<Hit, String>> SORTS = Map.of(
            "type", Hit::type, "name", Hit::name, "username", Hit::username, "email", Hit::email,
            "phone", Hit::phone, "role", Hit::role, "status", h -> h.active() ? "0" : "1");

    private final UserRepository users;
    private final MemberRepository members;

    public SearchService(UserRepository users, MemberRepository members) { this.users = users; this.members = members; }

    public Result search(String q, Scope scope, String sort, String dir, int page, int size) {
        Function<Hit, String> key = SORTS.get(sort);
        if (key == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Невалидна колона за сортиране");
        int pageSize = Math.min(Math.max(size, 1), 100);

        List<Hit> hits = new ArrayList<>();
        if (scope != Scope.MEMBERS)
            users.findAll().forEach(u -> hits.add(new Hit("USER", u.getId(), u.getFullName(), u.getUsername(),
                    u.getEmail(), u.getPhone(), u.getRole().name(), u.isActive())));
        if (scope != Scope.USERS)
            members.findAll().forEach(m -> hits.add(new Hit("MEMBER", m.getId(), m.getFullName(), null,
                    m.getEmail(), m.getPhone(), null, m.isActive())));

        String[] terms = q == null ? new String[0] : q.trim().toLowerCase(Locale.ROOT).split("\\s+");
        Comparator<String> order = String.CASE_INSENSITIVE_ORDER;
        if ("desc".equalsIgnoreCase(dir)) order = order.reversed();
        Comparator<Hit> cmp = Comparator.comparing(key, Comparator.nullsLast(order))
                .thenComparing(Hit::type).thenComparing(Hit::id); // tie-breakers keep paging stable

        List<Hit> all = hits.stream().filter(h -> matches(h, terms)).sorted(cmp).toList();
        int totalPages = (all.size() + pageSize - 1) / pageSize;
        int p = totalPages == 0 ? 0 : Math.min(Math.max(page, 0), totalPages - 1); // clamp out-of-range pages
        int from = Math.min(p * pageSize, all.size()), to = Math.min(from + pageSize, all.size());
        return new Result(all.subList(from, to), p, pageSize, all.size(), totalPages);
    }

    /** Every word of the query must appear somewhere in name, username, email or phone. */
    private static boolean matches(Hit h, String[] terms) {
        String haystack = String.join(" ", nz(h.name()), nz(h.username()), nz(h.email()), nz(h.phone())).toLowerCase(Locale.ROOT);
        for (String t : terms) if (!t.isEmpty() && !haystack.contains(t)) return false;
        return true;
    }

    private static String nz(String s) { return s == null ? "" : s; }
}
