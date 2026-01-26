package com.greenwich.flowerplus.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable // Đánh dấu: Đây không phải bảng riêng, mà là 1 phần của bảng khác
@Getter @Setter @NoArgsConstructor
@AllArgsConstructor @Builder
public class ShippingInfo {

    @Column(name = "weight_g")
    private Integer weightInGram;

    @Column(name = "length_cm")
    private Integer length;

    @Column(name = "width_cm")
    private Integer width;

    @Column(name = "height_cm")
    private Integer height;
}