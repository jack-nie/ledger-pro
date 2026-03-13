package com.example.ledgerpro.service;

import com.example.ledgerpro.dto.BudgetResponse;
import com.example.ledgerpro.dto.DashboardResponse;
import com.example.ledgerpro.dto.TransactionResponse;
import com.example.ledgerpro.model.Account;
import com.example.ledgerpro.model.TransactionRecord;
import com.example.ledgerpro.model.TransactionType;
import com.example.ledgerpro.repository.AccountRepository;
import com.example.ledgerpro.repository.TransactionRecordRepository;
import com.example.ledgerpro.support.PeriodSupport;
import com.example.ledgerpro.support.TransactionMetricsSupport;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final TransactionRecordRepository transactionRecordRepository;
    private final AccountRepository accountRepository;
    private final BudgetService budgetService;
    private final TransactionService transactionService;
    private final RecurringService recurringService;

    public DashboardService(TransactionRecordRepository transactionRecordRepository,
                            AccountRepository accountRepository,
                            BudgetService budgetService,
                            TransactionService transactionService,
                            RecurringService recurringService) {
        this.transactionRecordRepository = transactionRecordRepository;
        this.accountRepository = accountRepository;
        this.budgetService = budgetService;
        this.transactionService = transactionService;
        this.recurringService = recurringService;
    }

    @Cacheable(value = "dashboard", key = "#period == null || #period.trim().isEmpty() ? 'current' : #period")
    public DashboardResponse getDashboard(String period) {
        recurringService.processDueRules();

        String normalizedPeriod = PeriodSupport.normalize(period);
        YearMonth month = PeriodSupport.parse(normalizedPeriod);
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();

        List<TransactionRecord> periodTransactions = transactionRecordRepository.findByTransactionDateBetween(start, end);
        List<TransactionRecord> trendTransactions = transactionRecordRepository.findByTransactionDateBetween(
                month.minusMonths(5).atDay(1),
                end
        );
        List<TransactionRecord> accountTransactions = transactionRecordRepository.findByTransactionDateLessThanEqual(end);
        List<Account> accounts = accountRepository.findAllByOrderBySortOrderAscNameAsc();

        DashboardResponse response = new DashboardResponse();
        response.setPeriod(normalizedPeriod);
        response.setSummary(buildSummary(periodTransactions));
        List<DashboardResponse.CategoryBreakdown> categoryBreakdown = buildCategoryBreakdown(periodTransactions);
        response.setCategoryBreakdown(categoryBreakdown);
        response.setTrend(buildTrend(month, trendTransactions));
        response.setAccounts(buildAccountSnapshots(accounts, accountTransactions));
        response.setBudgets(budgetService.listBudgets(normalizedPeriod));
        response.setRecentTransactions(buildRecentTransactions(periodTransactions));
        response.setInsights(buildInsights(periodTransactions, categoryBreakdown, response.getBudgets(), response.getSummary()));
        return response;
    }

    private DashboardResponse.Summary buildSummary(List<TransactionRecord> transactions) {
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;

        for (TransactionRecord transaction : transactions) {
            if (transaction.getType() == TransactionType.INCOME) {
                income = income.add(transaction.getAmount());
            } else if (transaction.getType() == TransactionType.EXPENSE) {
                expense = expense.add(transaction.getAmount());
            }
        }

        DashboardResponse.Summary summary = new DashboardResponse.Summary();
        summary.setIncome(income);
        summary.setExpense(expense);
        summary.setBalance(income.subtract(expense));
        if (income.compareTo(BigDecimal.ZERO) > 0) {
            summary.setSavingsRate(income.subtract(expense)
                    .multiply(new BigDecimal("100"))
                    .divide(income, 1, RoundingMode.HALF_UP));
        } else {
            summary.setSavingsRate(BigDecimal.ZERO);
        }
        return summary;
    }

    private List<DashboardResponse.TrendPoint> buildTrend(YearMonth currentMonth, List<TransactionRecord> transactions) {
        Map<String, DashboardResponse.TrendPoint> points = new LinkedHashMap<String, DashboardResponse.TrendPoint>();
        for (int i = 5; i >= 0; i--) {
            String key = currentMonth.minusMonths(i).format(MONTH_FORMATTER);
            DashboardResponse.TrendPoint point = new DashboardResponse.TrendPoint();
            point.setMonth(key);
            point.setIncome(BigDecimal.ZERO);
            point.setExpense(BigDecimal.ZERO);
            points.put(key, point);
        }

        for (TransactionRecord transaction : transactions) {
            String key = YearMonth.from(transaction.getTransactionDate()).format(MONTH_FORMATTER);
            DashboardResponse.TrendPoint point = points.get(key);
            if (point == null) {
                continue;
            }
            if (transaction.getType() == TransactionType.INCOME) {
                point.setIncome(point.getIncome().add(transaction.getAmount()));
            } else if (transaction.getType() == TransactionType.EXPENSE) {
                point.setExpense(point.getExpense().add(transaction.getAmount()));
            }
        }

        return new ArrayList<DashboardResponse.TrendPoint>(points.values());
    }

    private List<DashboardResponse.CategoryBreakdown> buildCategoryBreakdown(List<TransactionRecord> transactions) {
        Map<Long, BigDecimal> spendingByCategory = TransactionMetricsSupport.accumulateByCategory(transactions, TransactionType.EXPENSE);
        Map<Long, DashboardResponse.CategoryBreakdown> breakDownMap = new LinkedHashMap<Long, DashboardResponse.CategoryBreakdown>();
        BigDecimal totalExpense = BigDecimal.ZERO;

        for (TransactionRecord transaction : transactions) {
            if (transaction.getType() != TransactionType.EXPENSE) {
                continue;
            }
            totalExpense = totalExpense.add(transaction.getAmount());
            if (TransactionMetricsSupport.hasSplits(transaction)) {
                for (com.example.ledgerpro.model.TransactionSplitItem splitItem : transaction.getSplitItems()) {
                    if (!breakDownMap.containsKey(splitItem.getCategory().getId())) {
                        DashboardResponse.CategoryBreakdown breakdown = new DashboardResponse.CategoryBreakdown();
                        breakdown.setCategoryId(splitItem.getCategory().getId());
                        breakdown.setCategoryName(splitItem.getCategory().getName());
                        breakdown.setCategoryColor(splitItem.getCategory().getColorHex());
                        breakDownMap.put(splitItem.getCategory().getId(), breakdown);
                    }
                }
                continue;
            }
            if (transaction.getCategory() != null && !breakDownMap.containsKey(transaction.getCategory().getId())) {
                DashboardResponse.CategoryBreakdown breakdown = new DashboardResponse.CategoryBreakdown();
                breakdown.setCategoryId(transaction.getCategory().getId());
                breakdown.setCategoryName(transaction.getCategory().getName());
                breakdown.setCategoryColor(transaction.getCategory().getColorHex());
                breakDownMap.put(transaction.getCategory().getId(), breakdown);
            }
        }

        List<DashboardResponse.CategoryBreakdown> responses = new ArrayList<DashboardResponse.CategoryBreakdown>();
        for (Map.Entry<Long, BigDecimal> entry : spendingByCategory.entrySet()) {
            DashboardResponse.CategoryBreakdown breakdown = breakDownMap.get(entry.getKey());
            if (breakdown == null) {
                continue;
            }
            breakdown.setAmount(entry.getValue());
            if (totalExpense.compareTo(BigDecimal.ZERO) > 0) {
                breakdown.setShare(entry.getValue()
                        .multiply(new BigDecimal("100"))
                        .divide(totalExpense, 1, RoundingMode.HALF_UP));
            } else {
                breakdown.setShare(BigDecimal.ZERO);
            }
            responses.add(breakdown);
        }

        Collections.sort(responses, new Comparator<DashboardResponse.CategoryBreakdown>() {
            @Override
            public int compare(DashboardResponse.CategoryBreakdown left, DashboardResponse.CategoryBreakdown right) {
                return right.getAmount().compareTo(left.getAmount());
            }
        });
        return responses;
    }

    private List<DashboardResponse.AccountSnapshot> buildAccountSnapshots(List<Account> accounts,
                                                                          List<TransactionRecord> transactions) {
        Map<Long, BigDecimal> balances = new LinkedHashMap<Long, BigDecimal>();
        for (Account account : accounts) {
            balances.put(account.getId(), account.getInitialBalance());
        }

        for (TransactionRecord transaction : transactions) {
            Long sourceId = transaction.getSourceAccount().getId();
            BigDecimal sourceBalance = balances.get(sourceId);
            if (transaction.getType() == TransactionType.INCOME) {
                balances.put(sourceId, sourceBalance.add(transaction.getAmount()));
            } else if (transaction.getType() == TransactionType.EXPENSE) {
                balances.put(sourceId, sourceBalance.subtract(transaction.getAmount()));
            } else if (transaction.getType() == TransactionType.TRANSFER && transaction.getTargetAccount() != null) {
                balances.put(sourceId, sourceBalance.subtract(transaction.getAmount()));
                Long targetId = transaction.getTargetAccount().getId();
                balances.put(targetId, balances.get(targetId).add(transaction.getAmount()));
            }
        }

        List<DashboardResponse.AccountSnapshot> snapshots = new ArrayList<DashboardResponse.AccountSnapshot>();
        for (Account account : accounts) {
            DashboardResponse.AccountSnapshot snapshot = new DashboardResponse.AccountSnapshot();
            snapshot.setId(account.getId());
            snapshot.setName(account.getName());
            snapshot.setType(account.getType().name());
            snapshot.setColorHex(account.getColorHex());
            snapshot.setBalance(balances.get(account.getId()));
            snapshots.add(snapshot);
        }
        return snapshots;
    }

    private List<TransactionResponse> buildRecentTransactions(List<TransactionRecord> transactions) {
        List<TransactionRecord> sorted = new ArrayList<TransactionRecord>(transactions);
        Collections.sort(sorted, new Comparator<TransactionRecord>() {
            @Override
            public int compare(TransactionRecord left, TransactionRecord right) {
                int compareDate = right.getTransactionDate().compareTo(left.getTransactionDate());
                if (compareDate != 0) {
                    return compareDate;
                }
                return right.getCreatedAt().compareTo(left.getCreatedAt());
            }
        });

        List<TransactionResponse> responses = new ArrayList<TransactionResponse>();
        int limit = Math.min(6, sorted.size());
        for (int i = 0; i < limit; i++) {
            responses.add(transactionService.toResponse(sorted.get(i)));
        }
        return responses;
    }

    private List<DashboardResponse.Insight> buildInsights(List<TransactionRecord> periodTransactions,
                                                          List<DashboardResponse.CategoryBreakdown> categoryBreakdown,
                                                          List<BudgetResponse> budgets,
                                                          DashboardResponse.Summary summary) {
        List<DashboardResponse.Insight> insights = new ArrayList<DashboardResponse.Insight>();

        if (!categoryBreakdown.isEmpty()) {
            DashboardResponse.CategoryBreakdown top = categoryBreakdown.get(0);
            insights.add(insight("支出重心",
                    "本月最大支出来自 " + top.getCategoryName() + "，占总支出的 " + top.getShare().toPlainString() + "%。"));
        }

        int riskyBudgets = 0;
        for (BudgetResponse budget : budgets) {
            if ("RISK".equals(budget.getStatus()) || "OVER".equals(budget.getStatus())) {
                riskyBudgets++;
            }
        }
        insights.add(insight("预算提醒",
                riskyBudgets == 0 ? "当前预算执行平稳，没有接近超支的分类。" : "当前有 " + riskyBudgets + " 个预算接近或已经超支。"));

        Map<LocalDate, BigDecimal> dailyExpense = new LinkedHashMap<LocalDate, BigDecimal>();
        for (TransactionRecord transaction : periodTransactions) {
            if (transaction.getType() != TransactionType.EXPENSE) {
                continue;
            }
            BigDecimal current = dailyExpense.containsKey(transaction.getTransactionDate())
                    ? dailyExpense.get(transaction.getTransactionDate())
                    : BigDecimal.ZERO;
            dailyExpense.put(transaction.getTransactionDate(), current.add(transaction.getAmount()));
        }

        if (!dailyExpense.isEmpty()) {
            BigDecimal average = summary.getExpense().divide(new BigDecimal(dailyExpense.size()), 1, RoundingMode.HALF_UP);
            insights.add(insight("消费节奏", "本月有 " + dailyExpense.size() + " 个消费日，平均每日支出 " + average.toPlainString() + " 元。"));
        } else {
            insights.add(insight("消费节奏", "本月还没有支出记录，可以先录入第一笔账单。"));
        }

        return insights;
    }

    private DashboardResponse.Insight insight(String title, String description) {
        DashboardResponse.Insight insight = new DashboardResponse.Insight();
        insight.setTitle(title);
        insight.setDescription(description);
        return insight;
    }
}
