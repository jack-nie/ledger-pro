package com.example.ledgerpro.dto;

import com.example.ledgerpro.model.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionResponse {

    private Long id;
    private TransactionType type;
    private BigDecimal amount;
    private LocalDate transactionDate;
    private Long sourceAccountId;
    private String sourceAccountName;
    private String sourceAccountColor;
    private Long targetAccountId;
    private String targetAccountName;
    private Long categoryId;
    private String categoryName;
    private String categoryColor;
    private String merchant;
    private String note;
    private String labels;
    private List<String> tags = new ArrayList<String>();
    private List<SplitItemResponse> splits = new ArrayList<SplitItemResponse>();
    private Boolean hasSplit;
    private String splitSummary;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Long getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(Long sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    public String getSourceAccountName() {
        return sourceAccountName;
    }

    public void setSourceAccountName(String sourceAccountName) {
        this.sourceAccountName = sourceAccountName;
    }

    public String getSourceAccountColor() {
        return sourceAccountColor;
    }

    public void setSourceAccountColor(String sourceAccountColor) {
        this.sourceAccountColor = sourceAccountColor;
    }

    public Long getTargetAccountId() {
        return targetAccountId;
    }

    public void setTargetAccountId(Long targetAccountId) {
        this.targetAccountId = targetAccountId;
    }

    public String getTargetAccountName() {
        return targetAccountName;
    }

    public void setTargetAccountName(String targetAccountName) {
        this.targetAccountName = targetAccountName;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryColor() {
        return categoryColor;
    }

    public void setCategoryColor(String categoryColor) {
        this.categoryColor = categoryColor;
    }

    public String getMerchant() {
        return merchant;
    }

    public void setMerchant(String merchant) {
        this.merchant = merchant;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<SplitItemResponse> getSplits() {
        return splits;
    }

    public void setSplits(List<SplitItemResponse> splits) {
        this.splits = splits;
    }

    public Boolean getHasSplit() {
        return hasSplit;
    }

    public void setHasSplit(Boolean hasSplit) {
        this.hasSplit = hasSplit;
    }

    public String getSplitSummary() {
        return splitSummary;
    }

    public void setSplitSummary(String splitSummary) {
        this.splitSummary = splitSummary;
    }
}
