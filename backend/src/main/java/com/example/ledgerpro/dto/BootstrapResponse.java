package com.example.ledgerpro.dto;

import java.util.List;

public class BootstrapResponse {

    private String currentPeriod;
    private List<OptionItem> accounts;
    private List<OptionItem> expenseCategories;
    private List<OptionItem> incomeCategories;

    public String getCurrentPeriod() {
        return currentPeriod;
    }

    public void setCurrentPeriod(String currentPeriod) {
        this.currentPeriod = currentPeriod;
    }

    public List<OptionItem> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<OptionItem> accounts) {
        this.accounts = accounts;
    }

    public List<OptionItem> getExpenseCategories() {
        return expenseCategories;
    }

    public void setExpenseCategories(List<OptionItem> expenseCategories) {
        this.expenseCategories = expenseCategories;
    }

    public List<OptionItem> getIncomeCategories() {
        return incomeCategories;
    }

    public void setIncomeCategories(List<OptionItem> incomeCategories) {
        this.incomeCategories = incomeCategories;
    }

    public static class OptionItem {
        private Long id;
        private String name;
        private String type;
        private String colorHex;
        private String iconKey;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getColorHex() {
            return colorHex;
        }

        public void setColorHex(String colorHex) {
            this.colorHex = colorHex;
        }

        public String getIconKey() {
            return iconKey;
        }

        public void setIconKey(String iconKey) {
            this.iconKey = iconKey;
        }
    }
}
