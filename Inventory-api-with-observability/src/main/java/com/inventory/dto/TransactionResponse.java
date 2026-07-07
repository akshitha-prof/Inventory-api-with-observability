package com.inventory.dto;

import com.inventory.model.Transaction;
import com.inventory.model.TransactionStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder
public class TransactionResponse {
    private Long id;
    private String userName;
    private String itemTitle;
    private Long itemId;
    private LocalDateTime borrowDate;
    private LocalDate dueDate;
    private LocalDateTime returnDate;
    private TransactionStatus status;
    private boolean overdue;

    public static TransactionResponse from(Transaction tx) {
        return TransactionResponse.builder()
                .id(tx.getId())
                .userName(tx.getUser().getFullName())
                .itemTitle(tx.getItem().getTitle())
                .itemId(tx.getItem().getId())
                .borrowDate(tx.getBorrowDate())
                .dueDate(tx.getDueDate())
                .returnDate(tx.getReturnDate())
                .status(tx.getStatus())
                .overdue(tx.isOverdue())
                .build();
    }
}
