package com.ecommerce.ordermanagement.mapper;

import com.ecommerce.ordermanagement.domain.entity.Category;
import com.ecommerce.ordermanagement.dto.category.CategoryRequest;
import com.ecommerce.ordermanagement.dto.category.CategoryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "parentId", source = "parent.id")
    CategoryResponse toResponse(Category category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    Category toEntity(CategoryRequest request);
}
