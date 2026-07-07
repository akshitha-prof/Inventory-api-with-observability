package com.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must be under 100 characters")
    private String name;

    @Size(max = 500, message = "Description must be under 500 characters")
    private String description;
}
