package com.greenwich.flowerplus.entity;


import com.greenwich.flowerplus.common.constant.CommonConfig;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Builder
@Entity
@Table(name = "contact_addresses")
public class ContactAddress extends BaseSoftDeleteEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Override
    public Long getId() {
        return id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private UserProfile userProfile;

    @Column(name = "recipient_name", length = CommonConfig.MAX_LENGTH_ADDRESS, nullable = false)
    private String recipientName;

    @Column(name = "phone_number", length = 15, nullable = false)
    private String phoneNumber;

    // Geographic data - matches AddressRequest structure
    @Column(name = "province_code", length = 20, nullable = false)
    private String provinceCode;

    @Column(name = "province_name", length = 255, nullable = false)
    private String provinceName;

    @Column(name = "ward_code", length = 20, nullable = false)
    private String wardCode;

    @Column(name = "ward_name", length = 255, nullable = false)
    private String wardName;

    @Column(name = "district_name", length = 255, nullable = false)
    private String districtName; // User manually entered

    @Column(name = "detail_address", length = 255, nullable = false)
    private String detailAddress; // Street address

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private boolean isDefault = false;

    @Column(name = "notes", length = 255)
    private String notes;
}
