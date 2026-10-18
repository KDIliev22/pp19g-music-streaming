package com.example.music.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** A member's ticket order for an event. */
@Entity
@Table(name = "ticket_order") // "order" is a reserved SQL word
public class TicketOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private Event event;
    @ManyToOne(optional = false) private Member member;
    @Column(nullable = false) private int quantity;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal total;
    @Column(nullable = false) private LocalDateTime createdAt;

    protected TicketOrder() {}
    public TicketOrder(Event event, Member member, int quantity, BigDecimal total, LocalDateTime createdAt) {
        this.event = event; this.member = member; this.quantity = quantity; this.total = total; this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Event getEvent() { return event; }
    public Member getMember() { return member; }
    public int getQuantity() { return quantity; }
    public BigDecimal getTotal() { return total; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
