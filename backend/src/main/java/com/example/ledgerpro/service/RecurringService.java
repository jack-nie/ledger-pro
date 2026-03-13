package com.example.ledgerpro.service;

import com.example.ledgerpro.dto.RecurringRuleRequest;
import com.example.ledgerpro.dto.RecurringRuleResponse;
import com.example.ledgerpro.dto.TransactionRequest;
import com.example.ledgerpro.model.Account;
import com.example.ledgerpro.model.Category;
import com.example.ledgerpro.model.CategoryType;
import com.example.ledgerpro.model.RecurringFrequency;
import com.example.ledgerpro.model.RecurringRule;
import com.example.ledgerpro.model.TransactionType;
import com.example.ledgerpro.repository.AccountRepository;
import com.example.ledgerpro.repository.CategoryRepository;
import com.example.ledgerpro.repository.RecurringRuleRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class RecurringService {

    private final RecurringRuleRepository recurringRuleRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionService transactionService;
    private final CacheManager cacheManager;

    public RecurringService(RecurringRuleRepository recurringRuleRepository,
                            AccountRepository accountRepository,
                            CategoryRepository categoryRepository,
                            TransactionService transactionService,
                            CacheManager cacheManager) {
        this.recurringRuleRepository = recurringRuleRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.transactionService = transactionService;
        this.cacheManager = cacheManager;
    }

    public List<RecurringRuleResponse> listRules() {
        return recurringRuleRepository.findAllByOrderByActiveDescNextRunDateAscTitleAsc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = {"dashboard"}, allEntries = true)
    @Transactional
    public RecurringRuleResponse createRule(RecurringRuleRequest request) {
        RecurringRule rule = new RecurringRule();
        applyRequest(rule, request);
        return toResponse(recurringRuleRepository.save(rule));
    }

    @CacheEvict(value = {"dashboard"}, allEntries = true)
    @Transactional
    public RecurringRuleResponse updateRule(Long id, RecurringRuleRequest request) {
        RecurringRule rule = recurringRuleRepository.findById(id)
                .orElseThrow(notFound("定时记账规则不存在"));
        applyRequest(rule, request);
        return toResponse(recurringRuleRepository.save(rule));
    }

    @CacheEvict(value = {"dashboard"}, allEntries = true)
    @Transactional
    public void deleteRule(Long id) {
        if (!recurringRuleRepository.existsById(id)) {
            throw new NoSuchElementException("定时记账规则不存在");
        }
        recurringRuleRepository.deleteById(id);
    }

    @Scheduled(cron = "0 */30 * * * *")
    @Transactional
    public void scheduledProcessDueRules() {
        processDueRules();
    }

    @Transactional
    public int processDueRules() {
        int createdCount = 0;
        LocalDate today = LocalDate.now();

        for (RecurringRule rule : recurringRuleRepository.findDueRules(today)) {
            while (Boolean.TRUE.equals(rule.getActive()) && !rule.getNextRunDate().isAfter(today)) {
                if (rule.getEndDate() != null && rule.getNextRunDate().isAfter(rule.getEndDate())) {
                    rule.setActive(Boolean.FALSE);
                    break;
                }

                transactionService.create(buildTransactionRequest(rule));
                createdCount++;

                LocalDate nextDate = advance(rule.getNextRunDate(), rule.getFrequency());
                rule.setNextRunDate(nextDate);
                if (rule.getEndDate() != null && nextDate.isAfter(rule.getEndDate())) {
                    rule.setActive(Boolean.FALSE);
                }
            }
        }
        if (createdCount > 0) {
            Cache cache = cacheManager.getCache("dashboard");
            if (cache != null) {
                cache.clear();
            }
        }
        return createdCount;
    }

    private TransactionRequest buildTransactionRequest(RecurringRule rule) {
        TransactionRequest request = new TransactionRequest();
        request.setType(rule.getType());
        request.setAmount(rule.getAmount());
        request.setTransactionDate(rule.getNextRunDate());
        request.setSourceAccountId(rule.getSourceAccount().getId());
        request.setTargetAccountId(rule.getTargetAccount() == null ? null : rule.getTargetAccount().getId());
        request.setCategoryId(rule.getCategory() == null ? null : rule.getCategory().getId());
        request.setMerchant(rule.getMerchant());
        request.setNote(rule.getNote());
        request.setLabels(rule.getLabels());
        request.setSplits(new ArrayList<com.example.ledgerpro.dto.SplitItemRequest>());
        return request;
    }

    private void applyRequest(RecurringRule rule, RecurringRuleRequest request) {
        Account sourceAccount = accountRepository.findById(request.getSourceAccountId())
                .orElseThrow(notFound("付款账户不存在"));

        rule.setTitle(request.getTitle().trim());
        rule.setType(request.getType());
        rule.setFrequency(request.getFrequency());
        rule.setStartDate(request.getStartDate());
        rule.setNextRunDate(request.getNextRunDate());
        rule.setEndDate(request.getEndDate());
        rule.setSourceAccount(sourceAccount);
        rule.setAmount(request.getAmount());
        rule.setMerchant(trimToNull(request.getMerchant()));
        rule.setNote(trimToNull(request.getNote()));
        rule.setLabels(normalizeLabels(request.getLabels()));
        rule.setActive(request.getActive() == null ? Boolean.TRUE : request.getActive());

        if (request.getType() == TransactionType.TRANSFER) {
            if (request.getTargetAccountId() == null) {
                throw new IllegalArgumentException("转账规则必须指定目标账户");
            }
            if (request.getTargetAccountId().equals(request.getSourceAccountId())) {
                throw new IllegalArgumentException("转出账户和转入账户不能相同");
            }
            Account targetAccount = accountRepository.findById(request.getTargetAccountId())
                    .orElseThrow(notFound("转入账户不存在"));
            rule.setTargetAccount(targetAccount);
            rule.setCategory(null);
        } else {
            if (request.getCategoryId() == null) {
                throw new IllegalArgumentException("收入或支出规则必须指定分类");
            }
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(notFound("分类不存在"));
            if (request.getType() == TransactionType.EXPENSE && category.getType() != CategoryType.EXPENSE) {
                throw new IllegalArgumentException("支出规则必须绑定支出分类");
            }
            if (request.getType() == TransactionType.INCOME && category.getType() != CategoryType.INCOME) {
                throw new IllegalArgumentException("收入规则必须绑定收入分类");
            }
            rule.setCategory(category);
            rule.setTargetAccount(null);
        }

        if (rule.getNextRunDate().isBefore(rule.getStartDate())) {
            throw new IllegalArgumentException("下次执行日期不能早于开始日期");
        }
        if (rule.getEndDate() != null && rule.getEndDate().isBefore(rule.getStartDate())) {
            throw new IllegalArgumentException("结束日期不能早于开始日期");
        }
    }

    private RecurringRuleResponse toResponse(RecurringRule rule) {
        RecurringRuleResponse response = new RecurringRuleResponse();
        response.setId(rule.getId());
        response.setTitle(rule.getTitle());
        response.setType(rule.getType());
        response.setFrequency(rule.getFrequency());
        response.setStartDate(rule.getStartDate());
        response.setNextRunDate(rule.getNextRunDate());
        response.setEndDate(rule.getEndDate());
        response.setSourceAccountId(rule.getSourceAccount().getId());
        response.setSourceAccountName(rule.getSourceAccount().getName());
        if (rule.getTargetAccount() != null) {
            response.setTargetAccountId(rule.getTargetAccount().getId());
            response.setTargetAccountName(rule.getTargetAccount().getName());
        }
        if (rule.getCategory() != null) {
            response.setCategoryId(rule.getCategory().getId());
            response.setCategoryName(rule.getCategory().getName());
        }
        response.setAmount(rule.getAmount());
        response.setMerchant(rule.getMerchant());
        response.setNote(rule.getNote());
        response.setLabels(rule.getLabels());
        response.setActive(rule.getActive());
        return response;
    }

    private LocalDate advance(LocalDate currentDate, RecurringFrequency frequency) {
        if (frequency == RecurringFrequency.WEEKLY) {
            return currentDate.plusWeeks(1);
        }
        return currentDate.plusMonths(1);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String normalizeLabels(String labels) {
        List<String> tags = transactionService.parseTags(labels);
        if (tags.isEmpty()) {
            return null;
        }
        return tags.stream().collect(Collectors.joining(","));
    }

    private Supplier<NoSuchElementException> notFound(final String message) {
        return new Supplier<NoSuchElementException>() {
            @Override
            public NoSuchElementException get() {
                return new NoSuchElementException(message);
            }
        };
    }
}
