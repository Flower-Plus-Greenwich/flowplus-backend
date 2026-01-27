package com.greenwich.flowerplus.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.greenwich.flowerplus.common.enums.MaterialType;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    private String name;
    private String unit;
    private String imageUrl;
    private BigDecimal costPrice;
    private BigDecimal sellingPrice;
    private MaterialType type;

    private AuditorResponse createdBy;
    private AuditorResponse updatedBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant updatedAt;
}
