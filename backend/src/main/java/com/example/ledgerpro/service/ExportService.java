package com.example.ledgerpro.service;

import com.example.ledgerpro.dto.BudgetResponse;
import com.example.ledgerpro.dto.DashboardResponse;
import com.example.ledgerpro.dto.TransactionResponse;
import com.example.ledgerpro.model.TransactionType;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ExportService {

    private final TransactionService transactionService;
    private final DashboardService dashboardService;
    private final RecurringService recurringService;

    public ExportService(TransactionService transactionService,
                         DashboardService dashboardService,
                         RecurringService recurringService) {
        this.transactionService = transactionService;
        this.dashboardService = dashboardService;
        this.recurringService = recurringService;
    }

    public byte[] exportTransactions(String period,
                                     TransactionType type,
                                     Long categoryId,
                                     String keyword) {
        recurringService.processDueRules();
        List<TransactionResponse> transactions = transactionService.listTransactions(period, type, categoryId, keyword);

        List<String> lines = new ArrayList<String>();
        lines.add(csvLine("日期", "类型", "金额", "付款账户", "目标账户", "分类", "商户", "备注", "标签", "分账明细"));
        for (TransactionResponse transaction : transactions) {
            lines.add(csvLine(
                    transaction.getTransactionDate() == null ? "" : transaction.getTransactionDate().toString(),
                    transaction.getType() == null ? "" : transaction.getType().name(),
                    transaction.getAmount() == null ? "" : transaction.getAmount().toPlainString(),
                    transaction.getSourceAccountName(),
                    transaction.getTargetAccountName(),
                    transaction.getCategoryName(),
                    transaction.getMerchant(),
                    transaction.getNote(),
                    transaction.getLabels(),
                    transaction.getSplitSummary()
            ));
        }
        return withBom(String.join("\n", lines));
    }

    public byte[] exportDashboard(String period) {
        recurringService.processDueRules();
        DashboardResponse dashboard = dashboardService.getDashboard(period);

        List<String> lines = new ArrayList<String>();
        lines.add(csvLine("模块", "字段", "值1", "值2", "值3"));
        lines.add(csvLine("概览", "月份", dashboard.getPeriod(), "", ""));
        lines.add(csvLine("概览", "收入", decimal(dashboard.getSummary().getIncome()), "", ""));
        lines.add(csvLine("概览", "支出", decimal(dashboard.getSummary().getExpense()), "", ""));
        lines.add(csvLine("概览", "结余", decimal(dashboard.getSummary().getBalance()), "", ""));
        lines.add(csvLine("概览", "储蓄率", decimal(dashboard.getSummary().getSavingsRate()), "", ""));

        for (DashboardResponse.CategoryBreakdown item : dashboard.getCategoryBreakdown()) {
            lines.add(csvLine("分类占比", item.getCategoryName(), decimal(item.getAmount()), decimal(item.getShare()), ""));
        }
        for (BudgetResponse budget : dashboard.getBudgets()) {
            lines.add(csvLine("预算", budget.getCategoryName(), decimal(budget.getAmount()), decimal(budget.getSpent()), budget.getStatus()));
        }
        for (DashboardResponse.AccountSnapshot account : dashboard.getAccounts()) {
            lines.add(csvLine("账户", account.getName(), account.getType(), decimal(account.getBalance()), ""));
        }
        for (TransactionResponse transaction : dashboard.getRecentTransactions()) {
            lines.add(csvLine("最近流水", transaction.getTransactionDate().toString(), transaction.getType().name(), decimal(transaction.getAmount()), transaction.getMerchant()));
        }
        return withBom(String.join("\n", lines));
    }

    private String decimal(java.math.BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }

    private String csvLine(String... values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append('"');
            builder.append((values[i] == null ? "" : values[i]).replace("\"", "\"\""));
            builder.append('"');
        }
        return builder.toString();
    }

    private byte[] withBom(String content) {
        byte[] body = content.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[body.length + 3];
        result[0] = (byte) 0xEF;
        result[1] = (byte) 0xBB;
        result[2] = (byte) 0xBF;
        System.arraycopy(body, 0, result, 3, body.length);
        return result;
    }
}
