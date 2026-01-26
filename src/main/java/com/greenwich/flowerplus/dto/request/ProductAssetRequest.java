package com.greenwich.flowerplus.dto.request;

import com.greenwich.flowerplus.common.enums.AssetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request DTO specifically for managing product assets.
 * Supports add, update, and remove operations.
 */
public record ProductAssetRequest(

        @Schema(description = "Operation type: ADD, REMOVE, REPLACE, SET_THUMBNAIL", example = "ADD")
        @NotNull(message = "Operation type is required")
        AssetOperation operation,

        @Schema(description = "List of assets to process")
        @Valid
        List<AssetItem> assets,

        @Schema(description = "Asset ID to set as thumbnail (for SET_THUMBNAIL operation)", example = "123")
        Long thumbnailAssetId

) {

    public enum AssetOperation {
        /**
         * Add new assets to existing ones
         */
        ADD,

        /**
         * Remove specific assets by ID
         */
        REMOVE,

        /**
         * Replace all assets with the provided list
         */
        REPLACE,

        /**
         * Set a specific asset as thumbnail
         */
        SET_THUMBNAIL,

        /**
         * Update positions/order of assets
         */
        REORDER
    }

    /**
     * Individual asset item for add/update operations.
     */
    public record AssetItem(

            @Schema(description = "Asset ID (for updates/removes)", example = "123")
            Long id,

            @Schema(description = "Asset URL", example = "https://res.cloudinary.com/...")
            String url,

            @Schema(description = "Public ID from storage service", example = "flowerplus-product-images/abc123")
            String publicId,

            @Schema(description = "Asset type", example = "IMAGE")
            @NotNull(message = "Asset type is required")
            AssetType type,

            @Schema(description = "Whether this is the thumbnail", example = "false")
            Boolean isThumbnail,

            @Schema(description = "Display position", example = "0")
            Integer position

    ) {}
}
