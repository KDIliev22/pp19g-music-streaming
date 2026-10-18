package com.example.music.repository;

import com.example.music.model.Event;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByEventDateBetweenOrderByEventDateAsc(LocalDate from, LocalDate to);
}
