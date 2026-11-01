package com.ecommerce.ordermanagement.mapper;

import com.ecommerce.ordermanagement.domain.entity.Product;
import com.ecommerce.ordermanagement.dto.product.ProductRequest;
import com.ecommerce.ordermanagement.dto.product.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    ProductResponse toResponse(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", expression = "java(request.active() == null || request.active())")
    Product toEntity(ProductRequest request);
}
