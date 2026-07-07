package com.inventory.service;

import com.inventory.dto.ItemRequest;
import com.inventory.dto.ItemResponse;
import com.inventory.exception.DuplicateResourceException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.model.Category;
import com.inventory.model.Item;
import com.inventory.repository.CategoryRepository;
import com.inventory.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;

    public Page<ItemResponse> getAllItems(Pageable pageable) {
        return itemRepository.findAll(pageable).map(ItemResponse::from);
    }

    public Page<ItemResponse> getItemsByCategory(Long categoryId, Pageable pageable) {
        return itemRepository.findByCategoryId(categoryId, pageable).map(ItemResponse::from);
    }

    public Page<ItemResponse> searchItems(String query, Pageable pageable) {
        return itemRepository.search(query, pageable).map(ItemResponse::from);
    }

    public ItemResponse getItemById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", id));
        return ItemResponse.from(item);
    }

    @Transactional
    public ItemResponse createItem(ItemRequest request) {
        if (itemRepository.existsByIsbn(request.getIsbn())) {
            throw new DuplicateResourceException("Item with ISBN '" + request.getIsbn() + "' already exists");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        Item item = Item.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .isbn(request.getIsbn())
                .totalCopies(request.getTotalCopies())
                .availableCopies(request.getTotalCopies())
                .category(category)
                .build();

        return ItemResponse.from(itemRepository.save(item));
    }

    @Transactional
    public ItemResponse updateItem(Long id, ItemRequest request) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setIsbn(request.getIsbn());
        item.setTotalCopies(request.getTotalCopies());
        item.setCategory(category);
        return ItemResponse.from(itemRepository.save(item));
    }

    @Transactional
    public void deleteItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item", id));
        itemRepository.delete(item);
    }
}
