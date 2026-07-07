package com.inventory.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ItemRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    @Size(max = 1000)
    private String description;

    @NotBlank(message = "ISBN is required")
    @Size(max = 50)
    private String isbn;

    @NotNull(message = "Total copies is required")
    @Min(value = 1, message = "Must have at least 1 copy")
    private Integer totalCopies;

    @NotNull(message = "Category ID is required")
    private Long categoryId;
}
