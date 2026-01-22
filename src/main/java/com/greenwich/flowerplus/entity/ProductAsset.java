package com.greenwich.flowerplus.entity;

import com.greenwich.flowerplus.common.enums.AssetType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "product_assets")
@Getter
@Setter
public class ProductAsset extends BaseSoftDeleteEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String url;

    @Column(name = "public_id")
    private String publicId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetType type; // Enum: IMAGE, VIDEO

    @Column(name = "is_thumbnail")
    private Boolean isThumbnail = false;

    private Integer position = 0;

    // Lưu thông số kỹ thuật (Video duration, file size...)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "meta_data", columnDefinition = "jsonb")
    private Map<String, Object> metaData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
}