package com.greenwich.flowerplus.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Long id;
    String cartToken;
    Integer totalItems;
    BigDecimal totalPrice;
    List<CartItemResponse> items;
}
