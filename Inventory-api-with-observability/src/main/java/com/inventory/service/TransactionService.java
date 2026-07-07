package com.inventory.service;

import com.inventory.dto.TransactionRequest;
import com.inventory.dto.TransactionResponse;
import com.inventory.exception.BusinessException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.model.*;
import com.inventory.repository.ItemRepository;
import com.inventory.repository.TransactionRepository;
import com.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    private static final int MAX_ACTIVE_BORROWS = 5;

    public Page<TransactionResponse> getUserTransactions(Long userId, Pageable pageable) {
        return transactionRepository.findByUserId(userId, pageable).map(TransactionResponse::from);
    }

    @Transactional
    public TransactionResponse borrowItem(String userEmail, TransactionRequest request) {
        AppUser user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("User not found"));

        Item item = itemRepository.findById(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item", request.getItemId()));

        // Business rule: check max active borrows
        long activeBorrows = transactionRepository.countByUserIdAndStatus(user.getId(), TransactionStatus.BORROWED);
        if (activeBorrows >= MAX_ACTIVE_BORROWS) {
            throw new BusinessException("Maximum active borrows (" + MAX_ACTIVE_BORROWS + ") reached. Return an item first.");
        }

        // Business rule: check availability
        if (!item.isAvailable()) {
            throw new BusinessException("Item '" + item.getTitle() + "' is not available for borrowing");
        }

        // Decrement available copies
        item.setAvailableCopies(item.getAvailableCopies() - 1);
        if (item.getAvailableCopies() == 0) {
            item.setStatus(ItemStatus.UNAVAILABLE);
        }
        itemRepository.save(item);

        // Create transaction
        Transaction tx = Transaction.builder()
                .user(user)
                .item(item)
                .borrowDate(LocalDateTime.now())
                .dueDate(LocalDate.now().plusDays(request.getBorrowDays()))
                .build();

        return TransactionResponse.from(transactionRepository.save(tx));
    }

    @Transactional
    public TransactionResponse returnItem(Long transactionId, String userEmail) {
        Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction", transactionId));

        if (!tx.getUser().getEmail().equals(userEmail)) {
            throw new BusinessException("You can only return items you borrowed");
        }

        if (tx.getStatus() == TransactionStatus.RETURNED) {
            throw new BusinessException("This item has already been returned");
        }

        tx.setReturnDate(LocalDateTime.now());
        tx.setStatus(TransactionStatus.RETURNED);

        // Increment available copies
        Item item = tx.getItem();
        item.setAvailableCopies(item.getAvailableCopies() + 1);
        if (item.getStatus() == ItemStatus.UNAVAILABLE) {
            item.setStatus(ItemStatus.AVAILABLE);
        }
        itemRepository.save(item);

        return TransactionResponse.from(transactionRepository.save(tx));
    }
}
