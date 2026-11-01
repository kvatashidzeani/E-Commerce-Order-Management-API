package com.ecommerce.ordermanagement.service;

import com.ecommerce.ordermanagement.domain.entity.Category;
import com.ecommerce.ordermanagement.dto.category.CategoryRequest;
import com.ecommerce.ordermanagement.dto.category.CategoryResponse;
import com.ecommerce.ordermanagement.exception.BusinessException;
import com.ecommerce.ordermanagement.exception.ResourceNotFoundException;
import com.ecommerce.ordermanagement.mapper.CategoryMapper;
import com.ecommerce.ordermanagement.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    public CategoryResponse findById(Long id) {
        return categoryMapper.toResponse(getCategory(id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new BusinessException("Category name already exists: " + request.name(), HttpStatus.CONFLICT);
        }

        Category category = categoryMapper.toEntity(request);
        if (request.parentId() != null) {
            category.setParent(getCategory(request.parentId()));
        }

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    public Category getCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
    }
}
