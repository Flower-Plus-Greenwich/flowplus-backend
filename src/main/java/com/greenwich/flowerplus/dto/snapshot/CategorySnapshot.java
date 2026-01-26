package com.greenwich.flowerplus.dto.snapshot;

import com.fasterxml.jackson.annotation.JsonFormat;

public record CategorySnapshot(
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long id,
        String categoryName
) {
}
