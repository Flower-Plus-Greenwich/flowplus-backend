package com.greenwich.flowerplus.dto.snapshot;

import lombok.Builder;
import java.io.Serializable;
import java.math.BigDecimal;

@Builder
public record OrderItemCustomConfig(
        ItemSnapshot arrangementStyle,
        ItemSnapshot flowerType,
        ItemSnapshot vase
) implements Serializable {

    // Inner Record
    @Builder
    public record ItemSnapshot(
            Long id,
            String name,
            BigDecimal unitPrice,
            BigDecimal unitCost
    ) implements Serializable {}
}