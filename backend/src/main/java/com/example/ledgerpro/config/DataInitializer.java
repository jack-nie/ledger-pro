package com.example.ledgerpro.config;

import com.example.ledgerpro.model.Account;
import com.example.ledgerpro.model.AccountType;
import com.example.ledgerpro.model.AppUser;
import com.example.ledgerpro.model.BudgetPlan;
import com.example.ledgerpro.model.Category;
import com.example.ledgerpro.model.CategoryType;
import com.example.ledgerpro.model.RecurringFrequency;
import com.example.ledgerpro.model.RecurringRule;
import com.example.ledgerpro.model.TransactionRecord;
import com.example.ledgerpro.model.TransactionSplitItem;
import com.example.ledgerpro.model.TransactionType;
import com.example.ledgerpro.repository.AccountRepository;
import com.example.ledgerpro.repository.AppUserRepository;
import com.example.ledgerpro.repository.BudgetPlanRepository;
import com.example.ledgerpro.repository.CategoryRepository;
import com.example.ledgerpro.repository.RecurringRuleRepository;
import com.example.ledgerpro.repository.TransactionRecordRepository;
import com.example.ledgerpro.support.PeriodSupport;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetPlanRepository budgetPlanRepository;
    private final TransactionRecordRepository transactionRecordRepository;
    private final RecurringRuleRepository recurringRuleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(AppUserRepository appUserRepository,
                           AccountRepository accountRepository,
                           CategoryRepository categoryRepository,
                           BudgetPlanRepository budgetPlanRepository,
                           TransactionRecordRepository transactionRecordRepository,
                           RecurringRuleRepository recurringRuleRepository,
                           PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
        this.budgetPlanRepository = budgetPlanRepository;
        this.transactionRecordRepository = transactionRecordRepository;
        this.recurringRuleRepository = recurringRuleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (appUserRepository.count() == 0) {
            seedUsers();
        }
        if (accountRepository.count() == 0) {
            seedAccounts();
        }
        if (categoryRepository.count() == 0) {
            seedCategories();
        }
        if (budgetPlanRepository.count() == 0) {
            seedBudgets();
        }
        if (transactionRecordRepository.count() == 0) {
            seedTransactions();
        }
        if (recurringRuleRepository.count() == 0) {
            seedRecurringRules();
        }
    }

    private void seedUsers() {
        AppUser user = new AppUser();
        user.setUsername("demo");
        user.setDisplayName("演示账本");
        user.setPasswordHash(passwordEncoder.encode("123456"));
        appUserRepository.save(user);
    }

    private void seedAccounts() {
        List<Account> accounts = new ArrayList<Account>();
        accounts.add(account("招商银行", AccountType.BANK, "#0f766e", new BigDecimal("12000"), 1));
        accounts.add(account("支付宝", AccountType.E_WALLET, "#2563eb", new BigDecimal("2800"), 2));
        accounts.add(account("微信钱包", AccountType.E_WALLET, "#16a34a", new BigDecimal("1500"), 3));
        accounts.add(account("现金", AccountType.CASH, "#f97316", new BigDecimal("600"), 4));
        accounts.add(account("招商信用卡", AccountType.CREDIT, "#7c3aed", new BigDecimal("0"), 5));
        accountRepository.saveAll(accounts);
    }

    private void seedCategories() {
        List<Category> categories = new ArrayList<Category>();
        categories.add(category("餐饮", CategoryType.EXPENSE, "utensils", "#ef4444", 1));
        categories.add(category("交通", CategoryType.EXPENSE, "train", "#f59e0b", 2));
        categories.add(category("住房", CategoryType.EXPENSE, "house", "#8b5cf6", 3));
        categories.add(category("购物", CategoryType.EXPENSE, "bag", "#ec4899", 4));
        categories.add(category("娱乐", CategoryType.EXPENSE, "film", "#06b6d4", 5));
        categories.add(category("医疗", CategoryType.EXPENSE, "shield", "#10b981", 6));
        categories.add(category("工资", CategoryType.INCOME, "wallet", "#22c55e", 7));
        categories.add(category("副业", CategoryType.INCOME, "sparkles", "#14b8a6", 8));
        categories.add(category("理财收益", CategoryType.INCOME, "trending-up", "#84cc16", 9));
        categoryRepository.saveAll(categories);
    }

    private void seedBudgets() {
        Map<String, Category> categories = categories();
        String currentPeriod = PeriodSupport.normalize(null);

        List<BudgetPlan> budgets = new ArrayList<BudgetPlan>();
        budgets.add(budget(currentPeriod, categories.get("餐饮"), new BigDecimal("2600")));
        budgets.add(budget(currentPeriod, categories.get("交通"), new BigDecimal("700")));
        budgets.add(budget(currentPeriod, categories.get("住房"), new BigDecimal("4200")));
        budgets.add(budget(currentPeriod, categories.get("购物"), new BigDecimal("1800")));
        budgets.add(budget(currentPeriod, categories.get("娱乐"), new BigDecimal("1200")));
        budgetPlanRepository.saveAll(budgets);
    }

    private void seedTransactions() {
        Map<String, Account> accounts = accounts();
        Map<String, Category> categories = categories();
        List<TransactionRecord> records = new ArrayList<TransactionRecord>();
        YearMonth current = YearMonth.now();

        for (int i = 5; i >= 0; i--) {
            YearMonth month = current.minusMonths(i);
            records.add(record(TransactionType.INCOME, new BigDecimal("18000"), month.atDay(1),
                    accounts.get("招商银行"), null, categories.get("工资"), "公司发薪", "月度工资到账", "工资,固定"));
            records.add(record(TransactionType.EXPENSE, new BigDecimal("4200"), month.atDay(3),
                    accounts.get("招商银行"), null, categories.get("住房"), "链家", "房租", "刚需"));
            records.add(record(TransactionType.TRANSFER, new BigDecimal("3000"), month.atDay(5),
                    accounts.get("招商银行"), accounts.get("支付宝"), null, "转入零钱", "银行卡转支付宝", "账户调拨"));
            records.add(record(TransactionType.EXPENSE, new BigDecimal("980"), month.atDay(8),
                    accounts.get("支付宝"), null, categories.get("餐饮"), "外卖与聚餐", "朋友聚餐", "餐饮"));
            records.add(record(TransactionType.EXPENSE, new BigDecimal("260"), month.atDay(12),
                    accounts.get("微信钱包"), null, categories.get("交通"), "地铁和打车", "通勤", "通勤"));
            records.add(record(TransactionType.EXPENSE, new BigDecimal("820"), month.atDay(16),
                    accounts.get("招商信用卡"), null, categories.get("购物"), "京东", "日用和数码", "购物"));
            records.add(record(TransactionType.EXPENSE, new BigDecimal("360"), month.atDay(22),
                    accounts.get("支付宝"), null, categories.get("娱乐"), "电影+咖啡", "周末放松", "娱乐"));
            records.add(record(TransactionType.INCOME, new BigDecimal("1800"), month.atDay(25),
                    accounts.get("招商银行"), null, categories.get("副业"), "咨询项目", "副业结算", "副业"));
        }

        TransactionRecord splitRecord = record(TransactionType.EXPENSE, new BigDecimal("188"), LocalDate.now().minusDays(3),
                accounts.get("支付宝"), null, null, "商超采购", "一单拆成餐饮和购物", "家庭,分账");
        splitRecord.getSplitItems().add(splitItem(splitRecord, categories.get("餐饮"), new BigDecimal("128"), "食材", 0));
        splitRecord.getSplitItems().add(splitItem(splitRecord, categories.get("购物"), new BigDecimal("60"), "日用品", 1));
        records.add(splitRecord);

        records.add(record(TransactionType.EXPENSE, new BigDecimal("79"), LocalDate.now(),
                accounts.get("微信钱包"), null, categories.get("交通"), "网约车", "赶项目会议", "临时出行"));
        records.add(record(TransactionType.INCOME, new BigDecimal("320"), LocalDate.now().minusDays(2),
                accounts.get("招商银行"), null, categories.get("理财收益"), "基金分红", "理财到账", "理财"));

        transactionRecordRepository.saveAll(records);
    }

    private void seedRecurringRules() {
        Map<String, Account> accounts = accounts();
        Map<String, Category> categories = categories();

        List<RecurringRule> rules = new ArrayList<RecurringRule>();
        rules.add(recurringRule("房租自动记账", TransactionType.EXPENSE, RecurringFrequency.MONTHLY,
                LocalDate.now().withDayOfMonth(1),
                LocalDate.now().plusMonths(1).withDayOfMonth(3),
                accounts.get("招商银行"), null, categories.get("住房"),
                new BigDecimal("4200"), "链家", "下月房租", "固定,住房"));
        rules.add(recurringRule("健身周卡", TransactionType.EXPENSE, RecurringFrequency.WEEKLY,
                LocalDate.now(),
                nextWeekday(LocalDate.now(), DayOfWeek.MONDAY),
                accounts.get("支付宝"), null, categories.get("娱乐"),
                new BigDecimal("68"), "健身房", "每周训练", "健康,习惯"));
        recurringRuleRepository.saveAll(rules);
    }

    private Account account(String name, AccountType type, String color, BigDecimal initialBalance, int sortOrder) {
        Account account = new Account();
        account.setName(name);
        account.setType(type);
        account.setColorHex(color);
        account.setInitialBalance(initialBalance);
        account.setSortOrder(sortOrder);
        return account;
    }

    private Category category(String name, CategoryType type, String iconKey, String colorHex, int sortOrder) {
        Category category = new Category();
        category.setName(name);
        category.setType(type);
        category.setIconKey(iconKey);
        category.setColorHex(colorHex);
        category.setSortOrder(sortOrder);
        return category;
    }

    private BudgetPlan budget(String period, Category category, BigDecimal amount) {
        BudgetPlan budget = new BudgetPlan();
        budget.setPeriod(period);
        budget.setCategory(category);
        budget.setAmount(amount);
        return budget;
    }

    private TransactionRecord record(TransactionType type,
                                     BigDecimal amount,
                                     LocalDate date,
                                     Account source,
                                     Account target,
                                     Category category,
                                     String merchant,
                                     String note,
                                     String labels) {
        TransactionRecord record = new TransactionRecord();
        record.setType(type);
        record.setAmount(amount);
        record.setTransactionDate(date);
        record.setSourceAccount(source);
        record.setTargetAccount(target);
        record.setCategory(category);
        record.setMerchant(merchant);
        record.setNote(note);
        record.setLabels(labels);
        return record;
    }

    private TransactionSplitItem splitItem(TransactionRecord transaction,
                                           Category category,
                                           BigDecimal amount,
                                           String note,
                                           int sortOrder) {
        TransactionSplitItem splitItem = new TransactionSplitItem();
        splitItem.setTransaction(transaction);
        splitItem.setCategory(category);
        splitItem.setAmount(amount);
        splitItem.setNote(note);
        splitItem.setSortOrder(sortOrder);
        return splitItem;
    }

    private RecurringRule recurringRule(String title,
                                        TransactionType type,
                                        RecurringFrequency frequency,
                                        LocalDate startDate,
                                        LocalDate nextRunDate,
                                        Account source,
                                        Account target,
                                        Category category,
                                        BigDecimal amount,
                                        String merchant,
                                        String note,
                                        String labels) {
        RecurringRule rule = new RecurringRule();
        rule.setTitle(title);
        rule.setType(type);
        rule.setFrequency(frequency);
        rule.setStartDate(startDate);
        rule.setNextRunDate(nextRunDate);
        rule.setSourceAccount(source);
        rule.setTargetAccount(target);
        rule.setCategory(category);
        rule.setAmount(amount);
        rule.setMerchant(merchant);
        rule.setNote(note);
        rule.setLabels(labels);
        rule.setActive(Boolean.TRUE);
        return rule;
    }

    private LocalDate nextWeekday(LocalDate start, DayOfWeek target) {
        LocalDate current = start;
        while (current.getDayOfWeek() != target) {
            current = current.plusDays(1);
        }
        return current;
    }

    private Map<String, Account> accounts() {
        List<Account> accounts = accountRepository.findAll();
        Map<String, Account> map = new HashMap<String, Account>();
        for (Account account : accounts) {
            map.put(account.getName(), account);
        }
        return map;
    }

    private Map<String, Category> categories() {
        List<Category> categories = categoryRepository.findAll();
        Map<String, Category> map = new HashMap<String, Category>();
        for (Category category : categories) {
            map.put(category.getName(), category);
        }
        return map;
    }
}
