package com.example.ledgerpro.service;

import com.example.ledgerpro.dto.SplitItemRequest;
import com.example.ledgerpro.dto.SplitItemResponse;
import com.example.ledgerpro.dto.TransactionRequest;
import com.example.ledgerpro.dto.TransactionResponse;
import com.example.ledgerpro.model.Account;
import com.example.ledgerpro.model.Category;
import com.example.ledgerpro.model.CategoryType;
import com.example.ledgerpro.model.TransactionRecord;
import com.example.ledgerpro.model.TransactionSplitItem;
import com.example.ledgerpro.model.TransactionType;
import com.example.ledgerpro.repository.AccountRepository;
import com.example.ledgerpro.repository.CategoryRepository;
import com.example.ledgerpro.repository.TransactionRecordRepository;
import com.example.ledgerpro.support.PeriodSupport;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRecordRepository transactionRecordRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;

    public TransactionService(TransactionRecordRepository transactionRecordRepository,
                              AccountRepository accountRepository,
                              CategoryRepository categoryRepository) {
        this.transactionRecordRepository = transactionRecordRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<TransactionResponse> listTransactions(String period,
                                                      TransactionType type,
                                                      Long categoryId,
                                                      String keyword) {
        Specification<TransactionRecord> specification = buildSpecification(period, type, categoryId, keyword);
        Sort sort = Sort.by(Sort.Order.desc("transactionDate"), Sort.Order.desc("createdAt"));
        return transactionRecordRepository.findAll(specification, sort).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = {"dashboard"}, allEntries = true)
    @Transactional
    public TransactionResponse create(TransactionRequest request) {
        TransactionRecord record = new TransactionRecord();
        applyRequest(record, request);
        return toResponse(transactionRecordRepository.save(record));
    }

    @CacheEvict(value = {"dashboard"}, allEntries = true)
    @Transactional
    public TransactionResponse update(Long id, TransactionRequest request) {
        TransactionRecord record = transactionRecordRepository.findById(id)
                .orElseThrow(notFound("流水不存在"));
        applyRequest(record, request);
        return toResponse(transactionRecordRepository.save(record));
    }

    @CacheEvict(value = {"dashboard"}, allEntries = true)
    @Transactional
    public void delete(Long id) {
        if (!transactionRecordRepository.existsById(id)) {
            throw new NoSuchElementException("流水不存在");
        }
        transactionRecordRepository.deleteById(id);
    }

    public TransactionResponse toResponse(TransactionRecord record) {
        TransactionResponse response = new TransactionResponse();
        response.setId(record.getId());
        response.setType(record.getType());
        response.setAmount(record.getAmount());
        response.setTransactionDate(record.getTransactionDate());
        response.setSourceAccountId(record.getSourceAccount().getId());
        response.setSourceAccountName(record.getSourceAccount().getName());
        response.setSourceAccountColor(record.getSourceAccount().getColorHex());
        response.setMerchant(record.getMerchant());
        response.setNote(record.getNote());
        response.setLabels(record.getLabels());
        response.setTags(parseTags(record.getLabels()));

        if (record.getTargetAccount() != null) {
            response.setTargetAccountId(record.getTargetAccount().getId());
            response.setTargetAccountName(record.getTargetAccount().getName());
        }

        List<SplitItemResponse> splits = new ArrayList<SplitItemResponse>();
        if (record.getSplitItems() != null && !record.getSplitItems().isEmpty()) {
            List<TransactionSplitItem> sorted = new ArrayList<TransactionSplitItem>(record.getSplitItems());
            Collections.sort(sorted, new Comparator<TransactionSplitItem>() {
                @Override
                public int compare(TransactionSplitItem left, TransactionSplitItem right) {
                    return left.getSortOrder().compareTo(right.getSortOrder());
                }
            });

            for (TransactionSplitItem splitItem : sorted) {
                SplitItemResponse splitResponse = new SplitItemResponse();
                splitResponse.setId(splitItem.getId());
                splitResponse.setCategoryId(splitItem.getCategory().getId());
                splitResponse.setCategoryName(splitItem.getCategory().getName());
                splitResponse.setCategoryColor(splitItem.getCategory().getColorHex());
                splitResponse.setAmount(splitItem.getAmount());
                splitResponse.setNote(splitItem.getNote());
                splits.add(splitResponse);
            }
        }
        response.setSplits(splits);
        response.setHasSplit(!splits.isEmpty());
        response.setSplitSummary(buildSplitSummary(splits));

        if (record.getCategory() != null) {
            response.setCategoryId(record.getCategory().getId());
            response.setCategoryName(record.getCategory().getName());
            response.setCategoryColor(record.getCategory().getColorHex());
        } else if (!splits.isEmpty()) {
            if (splits.size() == 1) {
                response.setCategoryId(splits.get(0).getCategoryId());
                response.setCategoryName(splits.get(0).getCategoryName());
                response.setCategoryColor(splits.get(0).getCategoryColor());
            } else {
                response.setCategoryName("多分类分账");
                response.setCategoryColor(splits.get(0).getCategoryColor());
            }
        }

        return response;
    }

    private void applyRequest(TransactionRecord record, TransactionRequest request) {
        Account sourceAccount = accountRepository.findById(request.getSourceAccountId())
                .orElseThrow(notFound("付款账户不存在"));

        record.setType(request.getType());
        record.setAmount(request.getAmount());
        record.setTransactionDate(request.getTransactionDate());
        record.setSourceAccount(sourceAccount);
        record.setMerchant(trimToNull(request.getMerchant()));
        record.setNote(trimToNull(request.getNote()));
        record.setLabels(normalizeLabels(request.getLabels()));

        if (request.getType() == TransactionType.TRANSFER) {
            applyTransfer(record, request);
            return;
        }

        List<SplitItemRequest> splits = request.getSplits() == null
                ? new ArrayList<SplitItemRequest>()
                : request.getSplits();

        if (!splits.isEmpty()) {
            applySplitRecord(record, request, splits);
            record.setTargetAccount(null);
            return;
        }

        if (request.getCategoryId() == null) {
            throw new IllegalArgumentException("收入或支出必须指定分类");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(notFound("分类不存在"));
        validateCategoryType(category, request.getType());

        clearSplits(record);
        record.setCategory(category);
        record.setTargetAccount(null);
    }

    private void applyTransfer(TransactionRecord record, TransactionRequest request) {
        if (request.getTargetAccountId() == null) {
            throw new IllegalArgumentException("转账必须指定目标账户");
        }
        if (request.getTargetAccountId().equals(request.getSourceAccountId())) {
            throw new IllegalArgumentException("转出账户和转入账户不能相同");
        }
        if (request.getCategoryId() != null || (request.getSplits() != null && !request.getSplits().isEmpty())) {
            throw new IllegalArgumentException("转账流水不能带分类或分账");
        }

        Account targetAccount = accountRepository.findById(request.getTargetAccountId())
                .orElseThrow(notFound("转入账户不存在"));
        clearSplits(record);
        record.setTargetAccount(targetAccount);
        record.setCategory(null);
    }

    private void applySplitRecord(TransactionRecord record,
                                  TransactionRequest request,
                                  List<SplitItemRequest> splits) {
        if (request.getCategoryId() != null) {
            throw new IllegalArgumentException("分账流水不允许再指定顶级分类");
        }

        BigDecimal total = BigDecimal.ZERO;
        clearSplits(record);

        for (int i = 0; i < splits.size(); i++) {
            SplitItemRequest splitRequest = splits.get(i);
            Category category = categoryRepository.findById(splitRequest.getCategoryId())
                    .orElseThrow(notFound("分账分类不存在"));
            validateCategoryType(category, request.getType());

            TransactionSplitItem splitItem = new TransactionSplitItem();
            splitItem.setTransaction(record);
            splitItem.setCategory(category);
            splitItem.setAmount(splitRequest.getAmount());
            splitItem.setNote(trimToNull(splitRequest.getNote()));
            splitItem.setSortOrder(i);
            record.getSplitItems().add(splitItem);
            total = total.add(splitRequest.getAmount());
        }

        if (total.compareTo(request.getAmount()) != 0) {
            throw new IllegalArgumentException("分账金额合计必须等于流水总金额");
        }
        record.setCategory(record.getSplitItems().size() == 1
                ? record.getSplitItems().get(0).getCategory()
                : null);
    }

    private void clearSplits(TransactionRecord record) {
        if (record.getSplitItems() == null) {
            record.setSplitItems(new ArrayList<TransactionSplitItem>());
            return;
        }
        record.getSplitItems().clear();
    }

    private void validateCategoryType(Category category, TransactionType type) {
        if (type == TransactionType.EXPENSE && category.getType() != CategoryType.EXPENSE) {
            throw new IllegalArgumentException("支出流水必须绑定支出分类");
        }
        if (type == TransactionType.INCOME && category.getType() != CategoryType.INCOME) {
            throw new IllegalArgumentException("收入流水必须绑定收入分类");
        }
    }

    private Specification<TransactionRecord> buildSpecification(String period,
                                                                TransactionType type,
                                                                Long categoryId,
                                                                String keyword) {
        return new Specification<TransactionRecord>() {
            @Override
            public Predicate toPredicate(javax.persistence.criteria.Root<TransactionRecord> root,
                                         javax.persistence.criteria.CriteriaQuery<?> query,
                                         javax.persistence.criteria.CriteriaBuilder cb) {
                query.distinct(true);

                List<Predicate> predicates = new ArrayList<Predicate>();
                Join<Object, Object> categoryJoin = null;
                Join<Object, Object> sourceAccountJoin = null;
                Join<Object, Object> splitJoin = null;
                Join<Object, Object> splitCategoryJoin = null;

                if (StringUtils.hasText(period)) {
                    YearMonth yearMonth = PeriodSupport.parse(period);
                    predicates.add(cb.between(root.get("transactionDate"),
                            yearMonth.atDay(1),
                            yearMonth.atEndOfMonth()));
                }

                if (type != null) {
                    predicates.add(cb.equal(root.get("type"), type));
                }

                if (categoryId != null || StringUtils.hasText(keyword)) {
                    categoryJoin = root.join("category", JoinType.LEFT);
                    sourceAccountJoin = root.join("sourceAccount", JoinType.LEFT);
                    splitJoin = root.join("splitItems", JoinType.LEFT);
                    splitCategoryJoin = splitJoin.join("category", JoinType.LEFT);
                }

                if (categoryId != null) {
                    predicates.add(cb.or(
                            cb.equal(categoryJoin.get("id"), categoryId),
                            cb.equal(splitCategoryJoin.get("id"), categoryId)
                    ));
                }

                if (StringUtils.hasText(keyword)) {
                    String pattern = "%" + keyword.trim().toLowerCase() + "%";
                    predicates.add(cb.or(
                            cb.like(cb.lower(cb.coalesce(root.<String>get("merchant"), "")), pattern),
                            cb.like(cb.lower(cb.coalesce(root.<String>get("note"), "")), pattern),
                            cb.like(cb.lower(cb.coalesce(root.<String>get("labels"), "")), pattern),
                            cb.like(cb.lower(cb.coalesce(categoryJoin.<String>get("name"), "")), pattern),
                            cb.like(cb.lower(cb.coalesce(sourceAccountJoin.<String>get("name"), "")), pattern),
                            cb.like(cb.lower(cb.coalesce(splitCategoryJoin.<String>get("name"), "")), pattern),
                            cb.like(cb.lower(cb.coalesce(splitJoin.<String>get("note"), "")), pattern)
                    ));
                }

                return cb.and(predicates.toArray(new Predicate[0]));
            }
        };
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String normalizeLabels(String labels) {
        List<String> tags = parseTags(labels);
        if (tags.isEmpty()) {
            return null;
        }
        return tags.stream().collect(Collectors.joining(","));
    }

    public List<String> parseTags(String labels) {
        if (!StringUtils.hasText(labels)) {
            return new ArrayList<String>();
        }
        String[] parts = labels.replace('，', ',').split(",");
        Set<String> unique = new LinkedHashSet<String>();
        for (String part : parts) {
            if (StringUtils.hasText(part)) {
                unique.add(part.trim());
            }
        }
        return new ArrayList<String>(unique);
    }

    private String buildSplitSummary(List<SplitItemResponse> splits) {
        if (splits == null || splits.isEmpty()) {
            return null;
        }
        StringJoiner joiner = new StringJoiner(" / ");
        int limit = Math.min(3, splits.size());
        for (int i = 0; i < limit; i++) {
            SplitItemResponse split = splits.get(i);
            joiner.add(split.getCategoryName() + " " + split.getAmount().stripTrailingZeros().toPlainString());
        }
        if (splits.size() > limit) {
            joiner.add("...");
        }
        return joiner.toString();
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
