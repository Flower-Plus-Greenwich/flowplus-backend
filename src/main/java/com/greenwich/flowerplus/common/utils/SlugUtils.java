package com.greenwich.flowerplus.common.utils;

import com.github.slugify.Slugify;

public class SlugUtils {

    private SlugUtils() {
        // Utility class
    }

    private static final Slugify slg = Slugify.builder()
            .transliterator(true)
            .lowerCase(true)
            .build();

    public static String toSlug(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        return slg.slugify(input);
    }
}