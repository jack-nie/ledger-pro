package com.example.ledgerpro.repository;

import com.example.ledgerpro.model.RecurringRule;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecurringRuleRepository extends JpaRepository<RecurringRule, Long> {

    @Query("select r from RecurringRule r where r.active = true and r.nextRunDate <= :today order by r.nextRunDate asc")
    List<RecurringRule> findDueRules(@Param("today") LocalDate today);

    List<RecurringRule> findAllByOrderByActiveDescNextRunDateAscTitleAsc();
}
