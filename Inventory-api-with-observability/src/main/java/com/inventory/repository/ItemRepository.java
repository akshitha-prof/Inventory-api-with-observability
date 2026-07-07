package com.inventory.repository;

import com.inventory.model.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Page<Item> findByCategoryId(Long categoryId, Pageable pageable);
    boolean existsByIsbn(String isbn);

    @Query("SELECT i FROM Item i WHERE " +
           "LOWER(i.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(i.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Item> search(@Param("query") String query, Pageable pageable);

    long countByCategoryId(Long categoryId);
}
