package com.greenwich.flowerplus.mapper;

import com.greenwich.flowerplus.dto.request.CategoryRequest;
import com.greenwich.flowerplus.dto.response.CategoryPublicResponse;
import com.greenwich.flowerplus.dto.response.CategoryResponse;
import com.greenwich.flowerplus.entity.Category;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        uses = {AuditorMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

    @Mapping(target = "createdBy", source = "createdBy", qualifiedByName = "mapAuditor")
    @Mapping(target = "updatedBy", source = "updatedBy", qualifiedByName = "mapAuditor")
    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "children", source = "children")
    CategoryResponse toResponse(Category category);

    List<CategoryResponse> toResponseList(List<Category> categories);

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "children", source = "children")
    CategoryPublicResponse toPublicResponse(Category category);

    List<CategoryPublicResponse> toPublicResponseList(List<Category> categories);

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "productCategories", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    Category toEntity(CategoryRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "children", ignore = true)
    @Mapping(target = "productCategories", ignore = true)
    @Mapping(target = "deletedAt", ignore = true)
    void updateEntity(@MappingTarget Category category, CategoryRequest request);
}

