package com.example.ledgerpro.controller;

import com.example.ledgerpro.model.TransactionType;
import com.example.ledgerpro.service.ExportService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exports")
public class ExportController {

    private final ExportService exportService;

    public ExportController(ExportService exportService) {
        this.exportService = exportService;
    }

    @GetMapping(value = "/transactions.csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportTransactions(@RequestParam(required = false) String period,
                                                     @RequestParam(required = false) TransactionType type,
                                                     @RequestParam(required = false) Long categoryId,
                                                     @RequestParam(required = false) String keyword) {
        return csv("transactions-" + (period == null || period.trim().isEmpty() ? "all" : period) + ".csv",
                exportService.exportTransactions(period, type, categoryId, keyword));
    }

    @GetMapping(value = "/dashboard.csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportDashboard(@RequestParam(required = false) String period) {
        return csv("dashboard-" + (period == null || period.trim().isEmpty() ? "current" : period) + ".csv",
                exportService.exportDashboard(period));
    }

    private ResponseEntity<byte[]> csv(String filename, byte[] body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        return ResponseEntity.ok()
                .headers(headers)
                .body(body);
    }
}
