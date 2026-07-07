package com.inventory.controller;

import com.inventory.dto.ItemRequest;
import com.inventory.dto.ItemResponse;
import com.inventory.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
@Tag(name = "Items", description = "Item/inventory management endpoints")
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    @Operation(summary = "Get all items with pagination and sorting")
    public ResponseEntity<Page<ItemResponse>> getAll(
            @PageableDefault(size = 20, sort = "title") Pageable pageable) {
        return ResponseEntity.ok(itemService.getAllItems(pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Search items by title or description")
    public ResponseEntity<Page<ItemResponse>> search(
            @RequestParam String q,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(itemService.searchItems(q, pageable));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get items by category")
    public ResponseEntity<Page<ItemResponse>> getByCategory(
            @PathVariable Long categoryId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(itemService.getItemsByCategory(categoryId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get item by ID")
    public ResponseEntity<ItemResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(itemService.getItemById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new item")
    public ResponseEntity<ItemResponse> create(@Valid @RequestBody ItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(itemService.createItem(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing item")
    public ResponseEntity<ItemResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody ItemRequest request) {
        return ResponseEntity.ok(itemService.updateItem(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an item (admin only)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
}
