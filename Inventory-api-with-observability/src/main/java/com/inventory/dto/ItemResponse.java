package com.inventory.dto;

import com.inventory.model.Item;
import com.inventory.model.ItemStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class ItemResponse {
    private Long id;
    private String title;
    private String description;
    private String isbn;
    private Integer totalCopies;
    private Integer availableCopies;
    private ItemStatus status;
    private String categoryName;
    private Long categoryId;
    private LocalDateTime createdAt;

    public static ItemResponse from(Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .title(item.getTitle())
                .description(item.getDescription())
                .isbn(item.getIsbn())
                .totalCopies(item.getTotalCopies())
                .availableCopies(item.getAvailableCopies())
                .status(item.getStatus())
                .categoryName(item.getCategory().getName())
                .categoryId(item.getCategory().getId())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
