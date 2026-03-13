package com.example.ledgerpro.service;

import com.example.ledgerpro.dto.BootstrapResponse;
import com.example.ledgerpro.model.Account;
import com.example.ledgerpro.model.Category;
import com.example.ledgerpro.model.CategoryType;
import com.example.ledgerpro.repository.AccountRepository;
import com.example.ledgerpro.repository.CategoryRepository;
import com.example.ledgerpro.support.PeriodSupport;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class LookupService {

    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    public LookupService(AccountRepository accountRepository, CategoryRepository categoryRepository) {
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
    }

    @Cacheable(value = "lookups", key = "'bootstrap'")
    public BootstrapResponse getBootstrap() {
        BootstrapResponse response = new BootstrapResponse();
        response.setCurrentPeriod(PeriodSupport.normalize(null));
        response.setAccounts(accountRepository.findAllByOrderBySortOrderAscNameAsc().stream()
                .map(this::toOption)
                .collect(Collectors.toList()));
        response.setExpenseCategories(categoryRepository.findByTypeOrderBySortOrderAscNameAsc(CategoryType.EXPENSE).stream()
                .map(this::toOption)
                .collect(Collectors.toList()));
        response.setIncomeCategories(categoryRepository.findByTypeOrderBySortOrderAscNameAsc(CategoryType.INCOME).stream()
                .map(this::toOption)
                .collect(Collectors.toList()));
        return response;
    }

    private BootstrapResponse.OptionItem toOption(Account account) {
        BootstrapResponse.OptionItem item = new BootstrapResponse.OptionItem();
        item.setId(account.getId());
        item.setName(account.getName());
        item.setType(account.getType().name());
        item.setColorHex(account.getColorHex());
        return item;
    }

    private BootstrapResponse.OptionItem toOption(Category category) {
        BootstrapResponse.OptionItem item = new BootstrapResponse.OptionItem();
        item.setId(category.getId());
        item.setName(category.getName());
        item.setType(category.getType().name());
        item.setColorHex(category.getColorHex());
        item.setIconKey(category.getIconKey());
        return item;
    }
}
