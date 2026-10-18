package com.example.music.controller;

import com.example.music.dto.CreateOrder;
import com.example.music.dto.OrderDto;
import com.example.music.dto.OrderPage;
import com.example.music.service.OrderService;
import jakarta.validation.Valid;

import java.time.DayOfWeek;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** Reading orders: any logged-in user. Creating orders: ADMIN and RECEPTION (see SecurityConfig). */
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) { this.service = service; }

    /** GET /api/orders?week=42&year=2026&date=2026-10-15&day=MONDAY&hour=14&sort=createdAt&dir=desc&page=0&size=10 (all filters optional) */
    @GetMapping
    OrderPage list(@RequestParam(required = false) Integer year,
                   @RequestParam(required = false) Integer week,
                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                   @RequestParam(required = false) DayOfWeek day,
                   @RequestParam(required = false) Integer hour,
                   @RequestParam(defaultValue = "createdAt") String sort,
                   @RequestParam(defaultValue = "desc") String dir,
                   @RequestParam(defaultValue = "0") int page,
                   @RequestParam(defaultValue = "10") int size) {
        return service.find(year, week, date, day, hour, sort, dir, page, size);
    }

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    OrderDto create(@Valid @RequestBody CreateOrder body) { return service.create(body); }
}
