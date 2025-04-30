package com.ibrahim.banking.store_of_value_service.repository;

import com.ibrahim.banking.store_of_value_service.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);
    
    // find all accounts owned by a user
    List<Account> findByProfileId(String profileId);

} 