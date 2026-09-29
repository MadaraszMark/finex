package hu.finex.main.mapper;

import hu.finex.main.dto.CreateCategoryRequest;
import hu.finex.main.dto.CategoryResponse;
import hu.finex.main.dto.UpdateCategoryRequest;
import hu.finex.main.model.Category;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CategoryMapperTest {

    private final CategoryMapper mapper = new CategoryMapper();

    @Test
    void testToEntity() {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Food")
                .icon("🍔")
                .build();

        Category category = mapper.toEntity(request);

        assertNotNull(category);
        assertNull(category.getId());
        assertEquals("Food", category.getName());
        assertEquals("🍔", category.getIcon());
    }

    @Test
    void testUpdateEntity() {
        Category category = Category.builder()
                .id(5L)
                .name("Old name")
                .icon("❌")
                .build();

        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Transport")
                .icon("🚗")
                .build();

        mapper.updateEntity(category, request);

        assertEquals(5L, category.getId());
        assertEquals("Transport", category.getName());
        assertEquals("🚗", category.getIcon());
    }

    @Test
    void testUpdateEntity_fromUpdateRequest() {
        Category category = Category.builder()
                .id(6L)
                .name("Egyéb")
                .icon("circle")
                .build();

        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .name("Egyéb kiadás")
                .icon("circle-ellipsis")
                .build();

        mapper.updateEntity(category, request);

        assertEquals(6L, category.getId());
        assertEquals("Egyéb kiadás", category.getName());
        assertEquals("circle-ellipsis", category.getIcon());
    }

    @Test
    void testToResponse() {
        Category category = Category.builder()
                .id(12L)
                .name("Entertainment")
                .icon("🎬")
                .build();

        CategoryResponse response = mapper.toResponse(category);

        assertNotNull(response);
        assertEquals(12L, response.getId());
        assertEquals("Entertainment", response.getName());
        assertEquals("🎬", response.getIcon());
    }
}
