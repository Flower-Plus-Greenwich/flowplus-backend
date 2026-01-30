package com.greenwich.flowerplus.dto.request;

import jakarta.validation.constraints.NotNull;

public class CartCreateRequest {
    @NotNull
    private Long userId;

    private String cartToken;

}
