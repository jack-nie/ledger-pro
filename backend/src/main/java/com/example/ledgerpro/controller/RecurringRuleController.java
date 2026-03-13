package com.example.ledgerpro.controller;

import com.example.ledgerpro.dto.RecurringRuleRequest;
import com.example.ledgerpro.dto.RecurringRuleResponse;
import com.example.ledgerpro.service.RecurringService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recurring-rules")
public class RecurringRuleController {

    private final RecurringService recurringService;

    public RecurringRuleController(RecurringService recurringService) {
        this.recurringService = recurringService;
    }

    @GetMapping
    public List<RecurringRuleResponse> list() {
        return recurringService.listRules();
    }

    @PostMapping
    public RecurringRuleResponse create(@Valid @RequestBody RecurringRuleRequest request) {
        return recurringService.createRule(request);
    }

    @PutMapping("/{id}")
    public RecurringRuleResponse update(@PathVariable Long id, @Valid @RequestBody RecurringRuleRequest request) {
        return recurringService.updateRule(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        recurringService.deleteRule(id);
    }

    @PostMapping("/process")
    public Map<String, Object> process() {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("createdCount", recurringService.processDueRules());
        return response;
    }
}
