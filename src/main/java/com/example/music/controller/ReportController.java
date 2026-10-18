package com.example.music.controller;

import com.example.music.dto.Report;
import com.example.music.service.ReportPdfService;
import com.example.music.service.ReportService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService service;
    private final ReportPdfService pdfService;

    public ReportController(ReportService service, ReportPdfService pdfService) {
        this.service = service;
        this.pdfService = pdfService;
    }

    /** GET /api/reports?date=2026-10-18&months=6  (date defaults to today, months to 1) */
    @GetMapping
    Report report(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                @RequestParam(defaultValue = "1") int months) {
        return service.build(date != null ? date : LocalDate.now(), months);
    }

    /**
     * GET /api/reports/pdf?date=2026-10-18            -> shown inline in the browser's PDF viewer
     * GET /api/reports/pdf?date=2026-10-18&download=true -> downloaded as a file
     * The PDF contains all reports: 1, 6 and 12 months.
     */
    @GetMapping("/pdf")
    ResponseEntity<byte[]> pdf(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                               @RequestParam(defaultValue = "false") boolean download) {
        LocalDate d = date != null ? date : LocalDate.now();
        byte[] bytes = pdfService.generate(d);
        ContentDisposition disposition = (download ? ContentDisposition.attachment() : ContentDisposition.inline())
                .filename("otcheti-" + d + ".pdf").build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }
}
