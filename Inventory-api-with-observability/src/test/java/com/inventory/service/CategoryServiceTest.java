package com.inventory.service;

import com.inventory.dto.CategoryRequest;
import com.inventory.dto.CategoryResponse;
import com.inventory.exception.BusinessException;
import com.inventory.exception.DuplicateResourceException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.model.Category;
import com.inventory.repository.CategoryRepository;
import com.inventory.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private ItemRepository itemRepository;
    @InjectMocks private CategoryService categoryService;

    @Test
    void createCategory_success() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Fiction");
        request.setDescription("Fiction books");

        Category saved = Category.builder().name("Fiction").description("Fiction books").items(new ArrayList<>()).build();
        saved.setId(1L);

        when(categoryRepository.existsByNameIgnoreCase("Fiction")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryResponse response = categoryService.createCategory(request);

        assertThat(response.getName()).isEqualTo("Fiction");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_duplicateName_throwsException() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Fiction");

        when(categoryRepository.existsByNameIgnoreCase("Fiction")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void deleteCategory_withItems_throwsException() {
        Category category = Category.builder().name("Fiction").build();
        category.setId(1L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(itemRepository.countByCategoryId(1L)).thenReturn(5L);

        assertThatThrownBy(() -> categoryService.deleteCategory(1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("5 active items");
    }

    @Test
    void getCategoryById_notFound_throwsException() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
