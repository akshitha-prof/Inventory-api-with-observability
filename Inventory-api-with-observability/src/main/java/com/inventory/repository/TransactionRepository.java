package com.inventory.repository;

import com.inventory.model.Transaction;
import com.inventory.model.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Page<Transaction> findByUserId(Long userId, Pageable pageable);
    List<Transaction> findByItemIdAndStatus(Long itemId, TransactionStatus status);
    long countByUserIdAndStatus(Long userId, TransactionStatus status);
}
