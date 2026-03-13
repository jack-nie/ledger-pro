package com.example.ledgerpro.repository;

import com.example.ledgerpro.model.Category;
import com.example.ledgerpro.model.CategoryType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByTypeOrderBySortOrderAscNameAsc(CategoryType type);

    Optional<Category> findByName(String name);
}
