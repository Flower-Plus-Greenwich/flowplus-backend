package com.greenwich.flowerplus.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.greenwich.flowerplus.common.enums.ProductStatus;
import com.greenwich.flowerplus.dto.snapshot.CategorySnapshot;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseAdmin {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    private String name;
    private String slug;
    private String description;
    private BigDecimal basePrice;
    private BigDecimal costPrice;
    private BigDecimal originalPrice;

    private CategorySnapshot primaryCategory;
    private List<CategorySnapshot> categories;

    private ProductStatus status;

    private Integer weight;
    private Integer length;
    private Integer width;
    private Integer height;
    private boolean isMakeToOrder;

    // Danh sách assets (ảnh/video) của sản phẩm
    private List<AssetResponse> assets;

    private AuditorResponse createdBy;
    private AuditorResponse updatedBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant updatedAt;
}
