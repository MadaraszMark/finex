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

    @InjectMocks private TransactionCategoryService service;

    @Test
    void assignCategory_shouldThrowNotFound_whenTransactionMissing() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.assignCategory(1L, 2L));

        verify(transactionRepository).findById(1L);
        verifyNoInteractions(categoryRepository, transactionCategoryRepository, transactionCategoryMapper);
    }

    @Test
    void assignCategory_shouldThrowNotFound_whenCategoryMissing() {
        Transaction tx = Transaction.builder().id(1L).build();
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(tx));
        when(categoryRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.assignCategory(1L, 2L));

        verify(transactionRepository).findById(1L);
        verify(categoryRepository).findById(2L);
        verifyNoInteractions(transactionCategoryRepository, transactionCategoryMapper);
    }

    @Test
    void assignCategory_shouldThrowBusinessException_whenAlreadyExists() {
        Transaction tx = Transaction.builder().id(1L).build();
        Category cat = Category.builder().id(2L).build();

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(tx));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(cat));
        when(transactionCategoryRepository.existsByTransaction_IdAndCategory_Id(1L, 2L)).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.assignCategory(1L, 2L));

        verify(transactionCategoryRepository).existsByTransaction_IdAndCategory_Id(1L, 2L);
        verify(transactionCategoryRepository, never()).save(any());
        verifyNoInteractions(transactionCategoryMapper);
    }

    @Test
    void assignCategory_shouldSaveLink_andReturnResponse() {
        Transaction tx = Transaction.builder().id(1L).build();
        Category cat = Category.builder().id(2L).name("Food").icon("🍔").build();

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(tx));
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
    void listByTransaction_shouldThrowNotFound_whenTransactionMissing() {
        when(transactionRepository.existsById(5L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> service.listByTransaction(5L));

        verify(transactionRepository).existsById(5L);
        verifyNoInteractions(transactionCategoryRepository, transactionCategoryMapper);
    }

    @Test
    void listByTransaction_shouldReturnListItems() {
        when(transactionRepository.existsById(5L)).thenReturn(true);

        TransactionCategory tc1 = TransactionCategory.builder().id(1L).build();
        TransactionCategory tc2 = TransactionCategory.builder().id(2L).build();
        when(transactionCategoryRepository.findByTransaction_Id(5L)).thenReturn(List.of(tc1, tc2));

        TransactionCategoryListItemResponse r1 = TransactionCategoryListItemResponse.builder().build();
        TransactionCategoryListItemResponse r2 = TransactionCategoryListItemResponse.builder().build();
        when(transactionCategoryMapper.toListItem(tc1)).thenReturn(r1);
        when(transactionCategoryMapper.toListItem(tc2)).thenReturn(r2);

        List<TransactionCategoryListItemResponse> out = service.listByTransaction(5L);

        assertNotNull(out);
        assertEquals(2, out.size());
        assertSame(r1, out.get(0));
        assertSame(r2, out.get(1));

        verify(transactionCategoryRepository).findByTransaction_Id(5L);
        verify(transactionCategoryMapper).toListItem(tc1);
        verify(transactionCategoryMapper).toListItem(tc2);
    }

    @Test
    void listByCategory_shouldThrowNotFound_whenCategoryMissing() {
        when(categoryRepository.existsById(7L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> service.listByCategory(7L));

        verify(categoryRepository).existsById(7L);
        verifyNoInteractions(transactionCategoryRepository, transactionCategoryMapper);
    }

    @Test
    void listByCategory_shouldReturnResponses() {
        when(categoryRepository.existsById(7L)).thenReturn(true);

        TransactionCategory tc1 = TransactionCategory.builder().id(1L).build();
        TransactionCategory tc2 = TransactionCategory.builder().id(2L).build();
        when(transactionCategoryRepository.findByCategory_Id(7L)).thenReturn(List.of(tc1, tc2));

        TransactionCategoryResponse r1 = TransactionCategoryResponse.builder().id(1L).build();
        TransactionCategoryResponse r2 = TransactionCategoryResponse.builder().id(2L).build();
        when(transactionCategoryMapper.toResponse(tc1)).thenReturn(r1);
        when(transactionCategoryMapper.toResponse(tc2)).thenReturn(r2);

        List<TransactionCategoryResponse> out = service.listByCategory(7L);

        assertNotNull(out);
        assertEquals(2, out.size());
        assertSame(r1, out.get(0));
        assertSame(r2, out.get(1));

        verify(transactionCategoryRepository).findByCategory_Id(7L);
        verify(transactionCategoryMapper).toResponse(tc1);
        verify(transactionCategoryMapper).toResponse(tc2);
    }

    @Test
    void deleteRelation_shouldThrowNotFound_whenMissing() {
        when(transactionCategoryRepository.findById(9L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.deleteRelation(9L));

        verify(transactionCategoryRepository).findById(9L);
        verify(transactionCategoryRepository, never()).delete(any());
    }

    @Test
    void deleteRelation_shouldDelete_whenFound() {
        TransactionCategory link = TransactionCategory.builder().id(9L).build();
        when(transactionCategoryRepository.findById(9L)).thenReturn(Optional.of(link));

        service.deleteRelation(9L);

        verify(transactionCategoryRepository).findById(9L);
        verify(transactionCategoryRepository).delete(link);
        verifyNoInteractions(transactionCategoryMapper);
    }
}
