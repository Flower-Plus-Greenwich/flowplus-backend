package com.greenwich.flowerplus.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProductSort {

    NEWEST("newest"),
    NAME_ASC("name_asc"),
    NAME_DESC("name_desc"),
    PRICE_ASC("price_asc"),
    PRICE_DESC("price_desc"),
    BEST_SELLER("best_seller");

    private final String value;

    ProductSort(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ProductSort fromValue(String value) {
        for (ProductSort sort : values()) {
            if (sort.value.equalsIgnoreCase(value)) {
                return sort;
            }
        }
        throw new IllegalArgumentException("Unknown sort: " + value);
    }
}
