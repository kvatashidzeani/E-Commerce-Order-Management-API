package com.ecommerce.ordermanagement.service;

import com.ecommerce.ordermanagement.domain.entity.Category;
import com.ecommerce.ordermanagement.dto.category.CategoryRequest;
import com.ecommerce.ordermanagement.dto.category.CategoryResponse;
import com.ecommerce.ordermanagement.exception.BusinessException;
import com.ecommerce.ordermanagement.mapper.CategoryMapper;
import com.ecommerce.ordermanagement.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void findAllMapsResponses() {
        Category category = Category.builder().id(1L).name("Books").build();
        when(categoryRepository.findAll()).thenReturn(List.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(new CategoryResponse(1L, "Books", null));

        assertThat(categoryService.findAll()).containsExactly(new CategoryResponse(1L, "Books", null));
    }

    @Test
    void createRejectsDuplicateName() {
        when(categoryRepository.existsByNameIgnoreCase("Books")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(new CategoryRequest("Books", null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void createWithParent() {
        CategoryRequest request = new CategoryRequest("Novels", 1L);
        Category parent = Category.builder().id(1L).name("Books").build();
        Category entity = Category.builder().name("Novels").build();
        Category saved = Category.builder().id(2L).name("Novels").parent(parent).build();

        when(categoryRepository.existsByNameIgnoreCase("Novels")).thenReturn(false);
        when(categoryMapper.toEntity(request)).thenReturn(entity);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(parent));
        when(categoryRepository.save(entity)).thenReturn(saved);
        when(categoryMapper.toResponse(saved)).thenReturn(new CategoryResponse(2L, "Novels", 1L));

        CategoryResponse response = categoryService.create(request);

        assertThat(response.parentId()).isEqualTo(1L);
        assertThat(entity.getParent()).isEqualTo(parent);
        verifySave(entity);
    }

    private void verifySave(Category entity) {
        org.mockito.Mockito.verify(categoryRepository).save(any(Category.class));
        assertThat(entity.getParent()).isNotNull();
    }
}
