package com.greenwich.flowerplus.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "carts")
@Builder
public class Cart extends BaseTsidEntity{
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserProfile user;

    @OneToMany(
            mappedBy = "cart",            // Map ngược lại biến 'cart' bên CartItem
            cascade = CascadeType.ALL,    // Xóa Cart -> Xóa luôn Items
            orphanRemoval = true,         // Xóa Item khỏi List -> Xóa luôn trong DB
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();
}
