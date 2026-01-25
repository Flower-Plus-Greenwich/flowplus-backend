package com.greenwich.flowerplus.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.greenwich.flowerplus.common.enums.AssetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.Map;

@Builder
@Schema(description = "Product asset information (image or video)")
public record AssetResponse(
        @Schema(type = "string", description = "Asset ID", example = "1")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Long id,

        @Schema(description = "Asset URL", example = "https://example.com/image.jpg")
        String url,

        @Schema(description = "Public ID from storage service", example = "product/image_abc123")
        String publicId,

        @Schema(description = "Asset type (IMAGE or VIDEO)", example = "IMAGE")
        AssetType type,

        @Schema(description = "Whether this asset is the thumbnail", example = "true")
        Boolean isThumbnail,

        @Schema(description = "Display position/order", example = "0")
        Integer position,



        @Schema(description = "Metadata (video duration, file size, etc.)")
        Map<String, Object> metaData
) {
}
