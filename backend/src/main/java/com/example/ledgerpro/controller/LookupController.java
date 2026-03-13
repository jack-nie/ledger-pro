package com.example.ledgerpro.controller;

import com.example.ledgerpro.dto.BootstrapResponse;
import com.example.ledgerpro.service.LookupService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lookups")
public class LookupController {

    private final LookupService lookupService;

    public LookupController(LookupService lookupService) {
        this.lookupService = lookupService;
    }

    @GetMapping("/bootstrap")
    public BootstrapResponse bootstrap() {
        return lookupService.getBootstrap();
    }
}
