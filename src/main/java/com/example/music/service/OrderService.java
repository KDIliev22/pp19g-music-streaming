package com.example.music.service;

import com.example.music.dto.CreateOrder;
import com.example.music.dto.OrderDto;
import com.example.music.dto.OrderPage;
import com.example.music.model.Event;
import com.example.music.model.Member;
import com.example.music.model.TicketOrder;
import com.example.music.repository.EventRepository;
import com.example.music.repository.MemberRepository;
import com.example.music.repository.TicketOrderRepository;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.IsoFields;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OrderService {
    /** Whitelist of sortable columns. */
    private static final Map<String, Comparator<TicketOrder>> SORTS = Map.of(
            "createdAt", Comparator.comparing(TicketOrder::getCreatedAt),
            "total", Comparator.comparing(TicketOrder::getTotal),
            "quantity", Comparator.comparingInt(TicketOrder::getQuantity),
            "event", Comparator.<TicketOrder, String>comparing(o -> o.getEvent().getTitle(), String.CASE_INSENSITIVE_ORDER),
            "member", Comparator.<TicketOrder, String>comparing(o -> o.getMember().getFullName(), String.CASE_INSENSITIVE_ORDER));

    private final TicketOrderRepository orders;
    private final EventRepository events;
    private final MemberRepository members;

    public OrderService(TicketOrderRepository orders, EventRepository events, MemberRepository members) {
        this.orders = orders; this.events = events; this.members = members;
    }

    /**
     * Filters orders by ISO week (+year), exact date, day of week and hour of day - any combination,
     * every filter optional - then sorts and pages. Week and year default to the current ones when only a week is given.
     */
    public OrderPage find(Integer year, Integer week, LocalDate date, DayOfWeek day, Integer hour,
                          String sort, String dir, int page, int size) {
        Comparator<TicketOrder> cmp = SORTS.get(sort);
        if (cmp == null) throw bad("Невалидна колона за сортиране");
        if (week != null && (week < 1 || week > 53)) throw bad("Седмицата е между 1 и 53");
        if (hour != null && (hour < 0 || hour > 23)) throw bad("Часът е между 0 и 23");
        if ("desc".equalsIgnoreCase(dir)) cmp = cmp.reversed();
        cmp = cmp.thenComparing(TicketOrder::getId); // stable paging
        int pageSize = Math.min(Math.max(size, 1), 100);
        int weekYear = year != null ? year : LocalDate.now().get(IsoFields.WEEK_BASED_YEAR);

        List<TicketOrder> all = orders.findAll().stream()
                .filter(o -> matches(o.getCreatedAt(), weekYear, year, week, date, day, hour)).sorted(cmp).toList();
        int totalPages = (all.size() + pageSize - 1) / pageSize;
        int p = totalPages == 0 ? 0 : Math.min(Math.max(page, 0), totalPages - 1);
        int from = Math.min(p * pageSize, all.size()), to = Math.min(from + pageSize, all.size());
        BigDecimal sum = all.stream().map(TicketOrder::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new OrderPage(all.subList(from, to).stream().map(OrderDto::of).toList(), p, pageSize, all.size(), totalPages, sum);
    }

    @Transactional
    public OrderDto create(CreateOrder c) {
        Event event = events.findById(c.eventId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Събитието не е намерено"));
        Member member = members.findById(c.memberId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Членът не е намерен"));
        if (!member.isActive()) throw bad("Членът е неактивен");
        if (event.getEventDate().isBefore(LocalDate.now())) throw bad("Събитието вече е минало");
        event.sellTickets(c.quantity()); // keeps the reports module in sync with orders
        events.save(event);
        return OrderDto.of(orders.save(new TicketOrder(event, member, c.quantity(),
                event.getTicketPrice().multiply(BigDecimal.valueOf(c.quantity())), LocalDateTime.now())));
    }

    private static boolean matches(LocalDateTime t, int weekYear, Integer year, Integer week, LocalDate date, DayOfWeek day, Integer hour) {
        if (date != null && !t.toLocalDate().equals(date)) return false;
        if (week != null && (t.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) != week || t.get(IsoFields.WEEK_BASED_YEAR) != weekYear)) return false;
        if (week == null && year != null && t.get(IsoFields.WEEK_BASED_YEAR) != year) return false;
        if (day != null && t.getDayOfWeek() != day) return false;
        return hour == null || t.getHour() == hour;
    }

    private static ResponseStatusException bad(String msg) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg); }
}
