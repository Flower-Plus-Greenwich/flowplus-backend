package com.greenwich.flowerplus.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.greenwich.flowerplus.common.enums.CategoryType;
import lombok.*;

import java.util.List;

/**
 * Lightweight category response for public/storefront use.
 * Excludes admin audit fields (createdBy, updatedBy, createdAt, updatedAt).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryPublicResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    private String name;

    private String slug;

    private String description;

    private String thumbnail;

    private CategoryType type;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long parentId;

    private List<CategoryPublicResponse> children;
}
