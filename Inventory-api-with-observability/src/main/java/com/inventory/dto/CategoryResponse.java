package com.inventory.dto;

import com.inventory.model.Category;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private int itemCount;
    private LocalDateTime createdAt;

    public static CategoryResponse from(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .itemCount(category.getItems() != null ? category.getItems().size() : 0)
                .createdAt(category.getCreatedAt())
                .build();
    }
}
