package com.greenwich.flowerplus.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListingDto {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    private String name;
    private String slug;

    // Chỉ cần giá bán cơ bản (Frontend tự thêm chữ "Từ..." nếu cần)
    private BigDecimal price;

    // Chỉ cần tên Category để hiển thị nhãn (VD: "Áo thun")
    private String categoryName;

    // QUAN TRỌNG: Chỉ trả về 1 link ảnh đại diện (Thumbnail)
    // Thay vì trả về cả List<Asset> như ProductResponse
    private String thumbnail;

    // Có thể thêm rating nếu sau này làm review
    // private Double averageRating;
    // private Integer reviewCount;
    private Integer availableStock;
    private boolean inStock;
}