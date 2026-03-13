package com.example.ledgerpro.service;

import com.example.ledgerpro.dto.BudgetRequest;
import com.example.ledgerpro.dto.BudgetResponse;
import com.example.ledgerpro.model.BudgetPlan;
import com.example.ledgerpro.model.Category;
import com.example.ledgerpro.model.CategoryType;
import com.example.ledgerpro.repository.BudgetPlanRepository;
import com.example.ledgerpro.repository.CategoryRepository;
import com.example.ledgerpro.repository.TransactionRecordRepository;
import com.example.ledgerpro.support.PeriodSupport;
import com.example.ledgerpro.support.TransactionMetricsSupport;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BudgetService {

    private final BudgetPlanRepository budgetPlanRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRecordRepository transactionRecordRepository;

    public BudgetService(BudgetPlanRepository budgetPlanRepository,
                         CategoryRepository categoryRepository,
                         TransactionRecordRepository transactionRecordRepository) {
        this.budgetPlanRepository = budgetPlanRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRecordRepository = transactionRecordRepository;
    }

    public List<BudgetResponse> listBudgets(String period) {
        String normalizedPeriod = PeriodSupport.normalize(period);
        YearMonth yearMonth = PeriodSupport.parse(normalizedPeriod);
        Map<Long, BigDecimal> spendingByCategory = TransactionMetricsSupport.accumulateByCategory(
                transactionRecordRepository.findByTransactionDateBetween(yearMonth.atDay(1), yearMonth.atEndOfMonth()),
                com.example.ledgerpro.model.TransactionType.EXPENSE
        );

        List<BudgetResponse> responses = new ArrayList<BudgetResponse>();
        for (BudgetPlan plan : budgetPlanRepository.findByPeriod(normalizedPeriod)) {
            responses.add(toResponse(plan, spendingByCategory.getOrDefault(plan.getCategory().getId(), BigDecimal.ZERO)));
        }
        return responses;
    }

    @CacheEvict(value = {"dashboard"}, allEntries = true)
    @Transactional
    public BudgetResponse upsert(BudgetRequest request) {
        String normalizedPeriod = PeriodSupport.normalize(request.getPeriod());
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(notFound("预算分类不存在"));

        if (category.getType() != CategoryType.EXPENSE) {
            throw new IllegalArgumentException("预算只允许作用于支出分类");
        }

        BudgetPlan budgetPlan = budgetPlanRepository.findByPeriodAndCategoryId(normalizedPeriod, category.getId())
                .orElseGet(BudgetPlan::new);
        budgetPlan.setPeriod(normalizedPeriod);
        budgetPlan.setCategory(category);
        budgetPlan.setAmount(request.getAmount());

        BudgetPlan saved = budgetPlanRepository.save(budgetPlan);
        YearMonth yearMonth = PeriodSupport.parse(normalizedPeriod);
        Map<Long, BigDecimal> spendingByCategory = TransactionMetricsSupport.accumulateByCategory(
                transactionRecordRepository.findByTransactionDateBetween(yearMonth.atDay(1), yearMonth.atEndOfMonth()),
                com.example.ledgerpro.model.TransactionType.EXPENSE
        );

        return toResponse(saved, spendingByCategory.getOrDefault(category.getId(), BigDecimal.ZERO));
    }

    private BudgetResponse toResponse(BudgetPlan plan, BigDecimal spent) {
        BudgetResponse response = new BudgetResponse();
        response.setId(plan.getId());
        response.setPeriod(plan.getPeriod());
        response.setCategoryId(plan.getCategory().getId());
        response.setCategoryName(plan.getCategory().getName());
        response.setCategoryColor(plan.getCategory().getColorHex());
        response.setAmount(plan.getAmount());
        response.setSpent(spent);
        response.setRemaining(plan.getAmount().subtract(spent));

        BigDecimal usageRate = BigDecimal.ZERO;
        if (plan.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            usageRate = spent.multiply(new BigDecimal("100"))
                    .divide(plan.getAmount(), 1, RoundingMode.HALF_UP);
        }
        response.setUsageRate(usageRate);
        response.setStatus(resolveStatus(usageRate));
        return response;
    }

    private String resolveStatus(BigDecimal usageRate) {
        if (usageRate.compareTo(new BigDecimal("100")) > 0) {
            return "OVER";
        }
        if (usageRate.compareTo(new BigDecimal("85")) >= 0) {
            return "RISK";
        }
        return "SAFE";
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
