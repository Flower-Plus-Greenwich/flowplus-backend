package com.greenwich.flowerplus.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.greenwich.flowerplus.common.enums.CategoryType;
import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    private String name;

    private String slug;

    private String description;

    private String thumbnail;

    private CategoryType type;

    private Boolean isActive;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long parentId;

    private List<CategoryResponse> children;

    private AuditorResponse createdBy;
    private AuditorResponse updatedBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant updatedAt;
}
