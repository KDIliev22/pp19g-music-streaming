package com.example.music.dto;

import com.example.music.model.TicketOrder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderDto(Long id, Long eventId, String eventTitle, Long memberId, String memberName,
                       int quantity, BigDecimal total, LocalDateTime createdAt) {
    public static OrderDto of(TicketOrder o) {
        return new OrderDto(o.getId(), o.getEvent().getId(), o.getEvent().getTitle(), o.getMember().getId(),
                o.getMember().getFullName(), o.getQuantity(), o.getTotal(), o.getCreatedAt());
    }
}
