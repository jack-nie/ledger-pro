package com.example.ledgerpro.repository;

import com.example.ledgerpro.model.BudgetPlan;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BudgetPlanRepository extends JpaRepository<BudgetPlan, Long> {

    @Query("select b from BudgetPlan b join fetch b.category where b.period = :period order by b.category.sortOrder asc, b.category.name asc")
    List<BudgetPlan> findByPeriod(@Param("period") String period);

    @Query("select b from BudgetPlan b where b.period = :period and b.category.id = :categoryId")
    Optional<BudgetPlan> findByPeriodAndCategoryId(@Param("period") String period, @Param("categoryId") Long categoryId);
}
