package com.inventory.service;

import com.inventory.dto.TransactionRequest;
import com.inventory.dto.TransactionResponse;
import com.inventory.exception.BusinessException;
import com.inventory.model.*;
import com.inventory.repository.ItemRepository;
import com.inventory.repository.TransactionRepository;
import com.inventory.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private TransactionService transactionService;

    private AppUser testUser;
    private Item testItem;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder().name("Books").build();
        testCategory.setId(1L);

        testUser = AppUser.builder().email("test@test.com").fullName("Test User").password("encoded").build();
        testUser.setId(1L);

        testItem = Item.builder()
                .title("Clean Code").isbn("123").totalCopies(3).availableCopies(2)
                .status(ItemStatus.AVAILABLE).category(testCategory).build();
        testItem.setId(1L);
    }

    @Test
    void borrowItem_success() {
        TransactionRequest request = new TransactionRequest();
        request.setItemId(1L);

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(transactionRepository.countByUserIdAndStatus(1L, TransactionStatus.BORROWED)).thenReturn(0L);

        Transaction saved = Transaction.builder()
                .user(testUser).item(testItem)
                .borrowDate(LocalDateTime.now()).dueDate(LocalDate.now().plusDays(14))
                .build();
        saved.setId(1L);
        when(transactionRepository.save(any())).thenReturn(saved);
        when(itemRepository.save(any())).thenReturn(testItem);

        TransactionResponse response = transactionService.borrowItem("test@test.com", request);

        assertThat(response).isNotNull();
        assertThat(testItem.getAvailableCopies()).isEqualTo(1);
    }

    @Test
    void borrowItem_unavailable_throwsException() {
        testItem.setAvailableCopies(0);
        testItem.setStatus(ItemStatus.UNAVAILABLE);

        TransactionRequest request = new TransactionRequest();
        request.setItemId(1L);

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(transactionRepository.countByUserIdAndStatus(1L, TransactionStatus.BORROWED)).thenReturn(0L);

        assertThatThrownBy(() -> transactionService.borrowItem("test@test.com", request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void borrowItem_maxBorrowsReached_throwsException() {
        TransactionRequest request = new TransactionRequest();
        request.setItemId(1L);

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(testUser));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(transactionRepository.countByUserIdAndStatus(1L, TransactionStatus.BORROWED)).thenReturn(5L);

        assertThatThrownBy(() -> transactionService.borrowItem("test@test.com", request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Maximum active borrows");
    }

    @Test
    void returnItem_success() {
        Transaction tx = Transaction.builder()
                .user(testUser).item(testItem)
                .borrowDate(LocalDateTime.now().minusDays(7))
                .dueDate(LocalDate.now().plusDays(7))
                .status(TransactionStatus.BORROWED).build();
        tx.setId(1L);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(tx));
        when(transactionRepository.save(any())).thenReturn(tx);
        when(itemRepository.save(any())).thenReturn(testItem);

        TransactionResponse response = transactionService.returnItem(1L, "test@test.com");

        assertThat(response.getStatus()).isEqualTo(TransactionStatus.RETURNED);
    }

    @Test
    void returnItem_alreadyReturned_throwsException() {
        Transaction tx = Transaction.builder()
                .user(testUser).item(testItem).status(TransactionStatus.RETURNED).build();
        tx.setId(1L);

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(tx));

        assertThatThrownBy(() -> transactionService.returnItem(1L, "test@test.com"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already been returned");
    }
}
