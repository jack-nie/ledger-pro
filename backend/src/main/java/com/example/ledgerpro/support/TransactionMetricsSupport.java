package com.example.ledgerpro.support;

import com.example.ledgerpro.model.TransactionRecord;
import com.example.ledgerpro.model.TransactionSplitItem;
import com.example.ledgerpro.model.TransactionType;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TransactionMetricsSupport {

    private TransactionMetricsSupport() {
    }

    public static boolean hasSplits(TransactionRecord transaction) {
        return transaction.getSplitItems() != null && !transaction.getSplitItems().isEmpty();
    }

    public static Map<Long, BigDecimal> accumulateByCategory(List<TransactionRecord> transactions,
                                                             TransactionType type) {
        Map<Long, BigDecimal> result = new HashMap<Long, BigDecimal>();
        for (TransactionRecord transaction : transactions) {
            if (transaction.getType() != type) {
                continue;
            }
            if (hasSplits(transaction)) {
                for (TransactionSplitItem splitItem : transaction.getSplitItems()) {
                    Long categoryId = splitItem.getCategory().getId();
                    BigDecimal current = result.containsKey(categoryId) ? result.get(categoryId) : BigDecimal.ZERO;
                    result.put(categoryId, current.add(splitItem.getAmount()));
                }
                continue;
            }
            if (transaction.getCategory() == null) {
                continue;
            }
            Long categoryId = transaction.getCategory().getId();
            BigDecimal current = result.containsKey(categoryId) ? result.get(categoryId) : BigDecimal.ZERO;
            result.put(categoryId, current.add(transaction.getAmount()));
        }
        return result;
    }
}
