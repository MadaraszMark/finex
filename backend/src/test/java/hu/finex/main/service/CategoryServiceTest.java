package hu.finex.main.service;

import hu.finex.main.dto.CategoryResponse;
import hu.finex.main.dto.CreateCategoryRequest;
import hu.finex.main.dto.UpdateCategoryRequest;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.CategoryMapper;
import hu.finex.main.model.Category;
import hu.finex.main.repository.CategoryRepository;
import hu.finex.main.repository.TransactionCategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private TransactionCategoryRepository transactionCategoryRepository;
    @Mock private CategoryMapper categoryMapper;

    @InjectMocks private CategoryService service;

    @Test
    void create_shouldThrowBusinessException_whenNameExists() {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Food")
                .icon("🍔")
                .build();

        when(categoryRepository.existsByNameIgnoreCase("Food")).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.create(request));

        verify(categoryRepository).existsByNameIgnoreCase("Food");
        verifyNoInteractions(categoryMapper);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void create_shouldSaveAndReturnResponse_whenOk() {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Food")
                .icon("🍔")
                .build();

        when(categoryRepository.existsByNameIgnoreCase("Food")).thenReturn(false);

        Category mapped = Category.builder()
                .name("Food")
                .icon("🍔")
                .build();
        when(categoryMapper.toEntity(request)).thenReturn(mapped);

        Category saved = Category.builder()
                .id(10L)
                .name("Food")
                .icon("🍔")
                .build();
        when(categoryRepository.save(mapped)).thenReturn(saved);

        CategoryResponse expected = CategoryResponse.builder()
                .id(10L)
                .name("Food")
                .icon("🍔")
                .build();
        when(categoryMapper.toResponse(saved)).thenReturn(expected);

        CategoryResponse resp = service.create(request);

        assertNotNull(resp);
        assertEquals(10L, resp.getId());
        assertEquals("Food", resp.getName());
        assertEquals("🍔", resp.getIcon());

        verify(categoryRepository).existsByNameIgnoreCase("Food");
        verify(categoryMapper).toEntity(request);
        verify(categoryRepository).save(mapped);
        verify(categoryMapper).toResponse(saved);
    }

    @Test
    void getById_shouldReturnResponse() {
        Category category = Category.builder().id(5L).name("X").build();
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));

        CategoryResponse expected = CategoryResponse.builder().id(5L).name("X").build();
        when(categoryMapper.toResponse(category)).thenReturn(expected);

        CategoryResponse resp = service.getById(5L);

        assertNotNull(resp);
        assertEquals(5L, resp.getId());

        verify(categoryRepository).findById(5L);
        verify(categoryMapper).toResponse(category);
    }

    @Test
    void getById_shouldThrowNotFound_whenMissing() {
        when(categoryRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getById(5L));

        verify(categoryRepository).findById(5L);
        verifyNoInteractions(categoryMapper);
    }

    @Test
    void listAll_shouldMapAll() {
        Category c1 = Category.builder().id(1L).name("A").build();
        Category c2 = Category.builder().id(2L).name("B").build();
        when(categoryRepository.findAllByOrderByNameAsc()).thenReturn(List.of(c1, c2));

        CategoryResponse r1 = CategoryResponse.builder().id(1L).name("A").build();
        CategoryResponse r2 = CategoryResponse.builder().id(2L).name("B").build();
        when(categoryMapper.toResponse(c1)).thenReturn(r1);
        when(categoryMapper.toResponse(c2)).thenReturn(r2);

        List<CategoryResponse> out = service.listAll();

        assertNotNull(out);
        assertEquals(2, out.size());
        assertEquals(1L, out.get(0).getId());
        assertEquals(2L, out.get(1).getId());

        verify(categoryRepository).findAllByOrderByNameAsc();
        verify(categoryMapper).toResponse(c1);
        verify(categoryMapper).toResponse(c2);
    }

    @Test
    void delete_shouldThrowNotFound_whenMissing() {
        when(categoryRepository.findById(7L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.delete(7L));

        verify(categoryRepository).findById(7L);
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void delete_shouldDelete_whenFound() {
        Category category = Category.builder().id(7L).name("X").build();
        when(categoryRepository.findById(7L)).thenReturn(Optional.of(category));
        when(transactionCategoryRepository.existsByCategory_Id(7L)).thenReturn(false);

        service.delete(7L);

        verify(categoryRepository).findById(7L);
        verify(categoryRepository).delete(category);
        verifyNoInteractions(categoryMapper);
    }

    @Test
    void delete_shouldThrowBusinessException_whenCategoryIsInUse() {
        Category category = Category.builder().id(7L).name("X").build();
        when(categoryRepository.findById(7L)).thenReturn(Optional.of(category));
        when(transactionCategoryRepository.existsByCategory_Id(7L)).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.delete(7L));

        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void update_shouldRenameCategory_whenNameIsFree() {
        Category category = Category.builder().id(7L).name("Egyéb").icon("circle").build();
        when(categoryRepository.findById(7L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot("Egyéb kiadás", 7L)).thenReturn(false);

        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .name("Egyéb kiadás")
                .icon("circle-ellipsis")
                .build();

        CategoryResponse expected = CategoryResponse.builder().id(7L).name("Egyéb kiadás").build();
        when(categoryMapper.toResponse(category)).thenReturn(expected);

        CategoryResponse resp = service.update(7L, request);

        assertEquals(expected, resp);
        verify(categoryMapper).updateEntity(category, request);
    }

    @Test
    void update_shouldThrowBusinessException_whenNameBelongsToOtherCategory() {
        Category category = Category.builder().id(7L).name("Egyéb").build();
        when(categoryRepository.findById(7L)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot("Élelmiszer", 7L)).thenReturn(true);

        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .name("Élelmiszer")
                .icon("x")
                .build();

        assertThrows(BusinessException.class, () -> service.update(7L, request));

        verify(categoryMapper, never()).updateEntity(any(Category.class), any(UpdateCategoryRequest.class));
    }

    @Test
    void update_shouldThrowNotFound_whenMissing() {
        when(categoryRepository.findById(7L)).thenReturn(Optional.empty());

        UpdateCategoryRequest request = UpdateCategoryRequest.builder().name("X").icon("x").build();

        assertThrows(NotFoundException.class, () -> service.update(7L, request));
    }
}
