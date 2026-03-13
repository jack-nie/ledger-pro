package com.example.ledgerpro.repository;

import com.example.ledgerpro.model.Account;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findAllByOrderBySortOrderAscNameAsc();

    Optional<Account> findByName(String name);
}
