package com.greenwich.flowerplus.dto.response;

public record RoleResponse(
    Long id,
    String name,
    String description,
    String createdBy,
    String updatedBy) {
}
