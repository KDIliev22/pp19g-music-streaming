package com.example.music.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "music_event")
public class Event {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String title;
    @Column(nullable = false) private LocalDate eventDate;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal ticketPrice;
    @Column(nullable = false) private int ticketsSold;

    protected Event() {}
    public Event(Long id, String title, LocalDate eventDate, BigDecimal ticketPrice, int ticketsSold) {
        this.id = id; this.title = title; this.eventDate = eventDate; this.ticketPrice = ticketPrice; this.ticketsSold = ticketsSold;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public LocalDate getEventDate() { return eventDate; }
    public BigDecimal getTicketPrice() { return ticketPrice; }
    public int getTicketsSold() { return ticketsSold; }
    public void sellTickets(int n) { this.ticketsSold += n; }
}
