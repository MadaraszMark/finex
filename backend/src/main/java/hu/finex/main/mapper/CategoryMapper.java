package hu.finex.main.mapper;

import org.springframework.stereotype.Component;

import hu.finex.main.dto.CreateCategoryRequest;
import hu.finex.main.dto.CategoryResponse;
import hu.finex.main.model.Category;

@Component
public class CategoryMapper {

    public Category toEntity(CreateCategoryRequest request) {
        return Category.builder()
                .name(request.getName())
                .icon(request.getIcon())
                .build();
    }

    public void updateEntity(Category category, CreateCategoryRequest request) {
        category.setName(request.getName());
        category.setIcon(request.getIcon());
    }

    public CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .icon(category.getIcon())
                .build();
    }
}

