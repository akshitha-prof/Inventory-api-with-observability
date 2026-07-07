package com.inventory.controller;

import com.inventory.dto.TransactionRequest;
import com.inventory.dto.TransactionResponse;
import com.inventory.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Borrow and return operations")
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/my")
    @Operation(summary = "Get current user's transactions")
    public ResponseEntity<Page<TransactionResponse>> getMyTransactions(
            Authentication auth, @PageableDefault(size = 20) Pageable pageable) {
        // In production, extract user ID from auth; here we use a simplified approach
        return ResponseEntity.ok(transactionService.getUserTransactions(1L, pageable));
    }

    @PostMapping("/borrow")
    @Operation(summary = "Borrow an item")
    public ResponseEntity<TransactionResponse> borrow(
            Authentication auth, @Valid @RequestBody TransactionRequest request) {
        String email = (String) auth.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.borrowItem(email, request));
    }

    @PostMapping("/{id}/return")
    @Operation(summary = "Return a borrowed item")
    public ResponseEntity<TransactionResponse> returnItem(
            @PathVariable Long id, Authentication auth) {
        String email = (String) auth.getPrincipal();
        return ResponseEntity.ok(transactionService.returnItem(id, email));
    }
}
