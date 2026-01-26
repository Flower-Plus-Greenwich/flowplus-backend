package com.greenwich.flowerplus.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.greenwich.flowerplus.common.enums.AssetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

/**
 * Request DTO for product asset operations.
 */
public record AssetRequest(
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        @Schema(description = "Asset ID (null for new assets, present for updates)", example = "1")
        Long id,

        @Schema(description = "Asset URL", example = "https://example.com/image.jpg")
        @NotBlank(message = "{validation.asset.url.required}")
        String url,

        @Schema(description = "Public ID from storage service", example = "product/image_abc123")
        String publicId,

        @Schema(description = "Asset type (IMAGE or VIDEO)", example = "IMAGE")
        @NotNull(message = "{validation.asset.type.required}")
        AssetType type,

        @Schema(description = "Whether this asset is the thumbnail", example = "true")
        Boolean isThumbnail,

        @Schema(description = "Display position/order", example = "0")
        Integer position,


        @Schema(description = "Metadata (video duration, file size, etc.)")
        Map<String, Object> metaData
) {
}

