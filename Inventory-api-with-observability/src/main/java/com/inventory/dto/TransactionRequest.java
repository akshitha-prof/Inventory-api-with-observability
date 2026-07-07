package com.inventory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransactionRequest {
    @NotNull(message = "Item ID is required")
    private Long itemId;

    private Integer borrowDays = 14; // default 2-week loan period
}
