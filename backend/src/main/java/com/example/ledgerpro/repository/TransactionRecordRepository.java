package com.example.ledgerpro.repository;

import com.example.ledgerpro.model.TransactionRecord;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TransactionRecordRepository extends JpaRepository<TransactionRecord, Long>, JpaSpecificationExecutor<TransactionRecord> {

    List<TransactionRecord> findByTransactionDateBetween(LocalDate start, LocalDate end);

    List<TransactionRecord> findByTransactionDateLessThanEqual(LocalDate end);

    List<TransactionRecord> findTop8ByOrderByTransactionDateDescCreatedAtDesc();
}
