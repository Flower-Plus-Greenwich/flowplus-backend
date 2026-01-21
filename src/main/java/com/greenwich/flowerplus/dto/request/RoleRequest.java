package com.greenwich.flowerplus.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RoleRequest(
    @NotBlank(message = "Role name is required")
    String name,
    
    @NotBlank(message = "Role description is required")
    String description
) {}
