package com.greenwich.flowerplus.dto.snapshot;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Builder
public class OrderItemCustomConfig {
    private ItemSnapshot arrangementStyle;
    private ItemSnapshot flowerType;
    private ItemSnapshot vase;

    @Data
    @Builder
    public static class ItemSnapshot implements Serializable {
        private Long id;
        private String name;
        private BigDecimal price;
    }
}
