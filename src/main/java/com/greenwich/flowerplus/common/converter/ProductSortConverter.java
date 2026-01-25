package com.greenwich.flowerplus.common.converter;

import com.greenwich.flowerplus.common.enums.ProductSort;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class ProductSortConverter implements Converter<String, ProductSort> {

    @Override
    public ProductSort convert(String source) {
        return Arrays.stream(ProductSort.values())
                .filter(e -> e.getValue().equalsIgnoreCase(source))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid sort: " + source));
    }
}
