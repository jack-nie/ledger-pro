package com.example.ledgerpro.controller;

import com.example.ledgerpro.dto.BudgetRequest;
import com.example.ledgerpro.dto.BudgetResponse;
import com.example.ledgerpro.service.BudgetService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    public List<BudgetResponse> list(@RequestParam(required = false) String period) {
        return budgetService.listBudgets(period);
    }

    @PostMapping
    public BudgetResponse upsert(@Valid @RequestBody BudgetRequest request) {
        return budgetService.upsert(request);
    }
}
