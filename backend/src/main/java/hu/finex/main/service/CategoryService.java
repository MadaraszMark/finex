package hu.finex.main.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import hu.finex.main.dto.CategoryResponse;
import hu.finex.main.dto.CreateCategoryRequest;
import hu.finex.main.dto.UpdateCategoryRequest;
import hu.finex.main.exception.BusinessException;
import hu.finex.main.exception.NotFoundException;
import hu.finex.main.mapper.CategoryMapper;
import hu.finex.main.model.Category;
import hu.finex.main.repository.CategoryRepository;
import hu.finex.main.repository.TransactionCategoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionCategoryRepository transactionCategoryRepository;
    private final CategoryMapper categoryMapper;

    // Admin
    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BusinessException("Már létezik kategória ezzel a névvel.");
        }

        Category entity = categoryMapper.toEntity(request);
        entity = categoryRepository.save(entity);

        return categoryMapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Kategória nem található."));

        return categoryMapper.toResponse(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listAll() {
        return categoryRepository.findAllByOrderByNameAsc().stream().map(categoryMapper::toResponse).toList();
    }

    // Admin
    @Transactional
    public CategoryResponse update(Long id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Kategória nem található."));

        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
            throw new BusinessException("Már létezik kategória ezzel a névvel.");
        }

        categoryMapper.updateEntity(category, request);
        return categoryMapper.toResponse(category);
    }

    // Admin: tranzakcióhoz rendelt kategória nem törölhető (az adatbázis ON DELETE RESTRICT szabálya is ezt védi)
    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> new NotFoundException("Kategória nem található."));

        if (transactionCategoryRepository.existsByCategory_Id(id)) {
            throw new BusinessException("A kategória használatban van, ezért nem törölhető.");
        }

        categoryRepository.delete(category);
    }
}
