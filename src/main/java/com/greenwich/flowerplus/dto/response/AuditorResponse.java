package com.greenwich.flowerplus.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

@Builder
public record AuditorResponse(
        String username,
        String email,

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long id,
        String avatarUrl
) {
}
