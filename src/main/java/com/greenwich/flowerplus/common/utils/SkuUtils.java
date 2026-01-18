package com.greenwich.flowerplus.common.utils;

import java.util.Map;
import java.util.stream.Collectors;

public class SkuUtils {

    private SkuUtils() {
        // Utility class
    }

    public static String generateSku(String productSlug, Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            // Nếu ko có thuộc tính thì lấy slug + random
            return (productSlug + "-" + System.currentTimeMillis()).toUpperCase();
        }

        // Lấy các giá trị thuộc tính nối lại (White, M -> WHITE-M)
        String attributePart = attributes.values().stream()
                .map(SlugUtils::toSlug) // Slug hóa giá trị (tránh dấu tiếng việt)
                .collect(Collectors.joining("-"));

        // Kết quả: AO-GUCCI-WHITE-M
        return (productSlug + "-" + attributePart).toUpperCase();
    }
}
