package com.example.music.config;

import com.example.music.model.AppUser;
import com.example.music.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
public class SecurityConfig {

    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean UserDetailsService userDetailsService(UserRepository repo) {
        return name -> repo.findByUsername(name)
                .map(u -> User.withUsername(u.getUsername()).password(u.getPassword())
                        .roles(u.getRole().name()).disabled(!u.isActive()).build()) // disabled => login refused
                .orElseThrow(() -> new UsernameNotFoundException(name));
    }

    /** Session-cookie auth, so the browser's <audio> tag can stream without custom headers. */
    @Bean SecurityFilterChain chain(HttpSecurity http, UserRepository repo) throws Exception {
        return http
            .csrf(c -> c.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
            .authorizeHttpRequests(a -> a
                .requestMatchers("/", "/index.html", "/api/csrf", "/api/me").permitAll()
                .requestMatchers("/api/users/**", "/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/orders").hasAnyRole("ADMIN", "RECEPTION")
                .anyRequest().authenticated())
            .addFilterBefore(new FreshAccountFilter(repo), AuthorizationFilter.class)
            .formLogin(f -> f.successHandler((rq, rs, au) -> rs.setStatus(200))
                             .failureHandler((rq, rs, e) -> rs.setStatus(401)))
            .logout(l -> l.logoutSuccessHandler((rq, rs, au) -> rs.setStatus(200)))
            .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .build();
    }

    /**
     * Sessions would otherwise keep the role and active flag from login time. On every request this
     * re-reads the account: deactivated users are logged out at once, role changes apply at once.
     */
    private static class FreshAccountFilter extends OncePerRequestFilter {
        private final UserRepository repo;

        FreshAccountFilter(UserRepository repo) { this.repo = repo; }

        @Override
        protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
                throws ServletException, IOException {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
                AppUser u = repo.findByUsername(auth.getName()).orElse(null);
                if (u == null || !u.isActive()) {
                    SecurityContextHolder.clearContext();
                    HttpSession session = req.getSession(false);
                    if (session != null) session.invalidate();
                } else {
                    SecurityContextHolder.getContext().setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                            auth.getPrincipal(), null, List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name()))));
                }
            }
            chain.doFilter(req, res);
        }
    }
}
