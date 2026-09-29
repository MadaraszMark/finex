package hu.finex.main.service;

import hu.finex.main.dto.TransactionCategoryListItemResponse;
import hu.finex.main.dto.TransactionCategoryResponse;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.TransactionCategoryMapper;
import hu.finex.main.model.Category;
import hu.finex.main.model.Transaction;
import hu.finex.main.model.TransactionCategory;
import hu.finex.main.repository.CategoryRepository;
import hu.finex.main.repository.TransactionCategoryRepository;
import hu.finex.main.repository.TransactionRepository;
import hu.finex.main.security.CurrentUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionCategoryServiceTest {

    @Mock private TransactionCategoryRepository transactionCategoryRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private TransactionCategoryMapper transactionCategoryMapper;
    @Mock private CurrentUser currentUser;

    @InjectMocks private TransactionCategoryService service;

    @Test
    void assignCategory_shouldThrowNotFound_whenTransactionMissingOrNotOwn() {
        when(currentUser.requireId()).thenReturn(7L);
        when(transactionRepository.findByIdAndAccount_User_Id(1L, 7L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.assignCategory(1L, 2L));

        verify(transactionRepository).findByIdAndAccount_User_Id(1L, 7L);
        verifyNoInteractions(categoryRepository, transactionCategoryRepository, transactionCategoryMapper);
    }

    @Test
    void assignCategory_shouldThrowNotFound_whenCategoryMissing() {
        when(currentUser.requireId()).thenReturn(7L);

        Transaction tx = Transaction.builder().id(1L).build();
        when(transactionRepository.findByIdAndAccount_User_Id(1L, 7L)).thenReturn(Optional.of(tx));
        when(categoryRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.assignCategory(1L, 2L));

        verify(categoryRepository).findById(2L);
        verifyNoInteractions(transactionCategoryRepository, transactionCategoryMapper);
    }

    @Test
    void assignCategory_shouldThrowBusinessException_whenAlreadyExists() {
        when(currentUser.requireId()).thenReturn(7L);

        Transaction tx = Transaction.builder().id(1L).build();
        Category cat = Category.builder().id(2L).build();

        when(transactionRepository.findByIdAndAccount_User_Id(1L, 7L)).thenReturn(Optional.of(tx));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(cat));
        when(transactionCategoryRepository.existsByTransaction_IdAndCategory_Id(1L, 2L)).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.assignCategory(1L, 2L));

        verify(transactionCategoryRepository).existsByTransaction_IdAndCategory_Id(1L, 2L);
        verify(transactionCategoryRepository, never()).save(any());
        verifyNoInteractions(transactionCategoryMapper);
    }

    @Test
    void assignCategory_shouldSaveLink_andReturnResponse() {
        when(currentUser.requireId()).thenReturn(7L);

        Transaction tx = Transaction.builder().id(1L).build();
        Category cat = Category.builder().id(2L).name("Food").icon("🍔").build();

        when(transactionRepository.findByIdAndAccount_User_Id(1L, 7L)).thenReturn(Optional.of(tx));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(cat));
        when(transactionCategoryRepository.existsByTransaction_IdAndCategory_Id(1L, 2L)).thenReturn(false);

        TransactionCategory mapped = TransactionCategory.builder().transaction(tx).category(cat).build();
        when(transactionCategoryMapper.toEntity(tx, cat)).thenReturn(mapped);

        TransactionCategory saved = TransactionCategory.builder().id(10L).transaction(tx).category(cat).build();
        when(transactionCategoryRepository.save(mapped)).thenReturn(saved);

        TransactionCategoryResponse expected = TransactionCategoryResponse.builder()
                .id(10L)
                .transactionId(1L)
                .categoryId(2L)
                .categoryName("Food")
                .categoryIcon("🍔")
                .build();
        when(transactionCategoryMapper.toResponse(saved)).thenReturn(expected);

        TransactionCategoryResponse resp = service.assignCategory(1L, 2L);

        assertNotNull(resp);
        assertEquals(10L, resp.getId());
        assertEquals(1L, resp.getTransactionId());
        assertEquals(2L, resp.getCategoryId());
        assertEquals("Food", resp.getCategoryName());
        assertEquals("🍔", resp.getCategoryIcon());

        verify(transactionCategoryMapper).toEntity(tx, cat);
        verify(transactionCategoryRepository).save(mapped);
        verify(transactionCategoryMapper).toResponse(saved);
    }

    @Test
    void listByTransaction_shouldThrowNotFound_whenTransactionMissingOrNotOwn() {
        when(currentUser.requireId()).thenReturn(7L);
        when(transactionRepository.existsByIdAndAccount_User_Id(5L, 7L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> service.listByTransaction(5L));

        verifyNoInteractions(transactionCategoryRepository, transactionCategoryMapper);
    }

    @Test
    void listByTransaction_shouldReturnListItems() {
        when(currentUser.requireId()).thenReturn(7L);
        when(transactionRepository.existsByIdAndAccount_User_Id(5L, 7L)).thenReturn(true);

        TransactionCategory l1 = TransactionCategory.builder().id(1L).build();
        TransactionCategory l2 = TransactionCategory.builder().id(2L).build();
        when(transactionCategoryRepository.findByTransaction_Id(5L)).thenReturn(List.of(l1, l2));

        TransactionCategoryListItemResponse r1 = TransactionCategoryListItemResponse.builder().categoryId(1L).build();
        TransactionCategoryListItemResponse r2 = TransactionCategoryListItemResponse.builder().categoryId(2L).build();
        when(transactionCategoryMapper.toListItem(l1)).thenReturn(r1);
        when(transactionCategoryMapper.toListItem(l2)).thenReturn(r2);

        List<TransactionCategoryListItemResponse> out = service.listByTransaction(5L);

        assertEquals(2, out.size());
        assertEquals(1L, out.get(0).getCategoryId());
        assertEquals(2L, out.get(1).getCategoryId());
    }

    @Test
    void deleteRelation_shouldThrowNotFound_whenMissingOrNotOwn() {
        when(currentUser.requireId()).thenReturn(7L);
        when(transactionCategoryRepository.findByIdAndTransaction_Account_User_Id(9L, 7L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.deleteRelation(9L));

        verify(transactionCategoryRepository, never()).delete(any());
    }

    @Test
    void deleteRelation_shouldDelete_whenFound() {
        when(currentUser.requireId()).thenReturn(7L);

        TransactionCategory link = TransactionCategory.builder().id(9L).build();
        when(transactionCategoryRepository.findByIdAndTransaction_Account_User_Id(9L, 7L)).thenReturn(Optional.of(link));

        service.deleteRelation(9L);

        verify(transactionCategoryRepository).delete(link);
    }
}
