package com.example.ledgerpro.controller;

import com.example.ledgerpro.dto.TransactionRequest;
import com.example.ledgerpro.dto.TransactionResponse;
import com.example.ledgerpro.model.TransactionType;
import com.example.ledgerpro.service.RecurringService;
import com.example.ledgerpro.service.TransactionService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final RecurringService recurringService;

    public TransactionController(TransactionService transactionService,
                                 RecurringService recurringService) {
        this.transactionService = transactionService;
        this.recurringService = recurringService;
    }

    @GetMapping
    public List<TransactionResponse> list(@RequestParam(required = false) String period,
                                          @RequestParam(required = false) TransactionType type,
                                          @RequestParam(required = false) Long categoryId,
                                          @RequestParam(required = false) String keyword) {
        recurringService.processDueRules();
        return transactionService.listTransactions(period, type, categoryId, keyword);
    }

    @PostMapping
    public TransactionResponse create(@Valid @RequestBody TransactionRequest request) {
        return transactionService.create(request);
    }

    @PutMapping("/{id}")
    public TransactionResponse update(@PathVariable Long id, @Valid @RequestBody TransactionRequest request) {
        return transactionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        transactionService.delete(id);
    }
}
