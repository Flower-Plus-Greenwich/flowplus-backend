package com.greenwich.flowerplus.dto.snapshot;

import com.greenwich.flowerplus.entity.ContactAddress;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShippingAddressSnapshot implements Serializable {
    private String fullName;
    private String phoneNumber;
    private String province;
    private String district;
    private String ward;
    private String specificAddress;

    public String toFullString() {
        return String.format("%s, %s, %s, %s", specificAddress, ward, district, province);
    }

    public static ShippingAddressSnapshot fromAddress(ContactAddress address) {
        if (address == null) return null;

        return ShippingAddressSnapshot.builder()
                .fullName(address.getRecipientName())
                .phoneNumber(address.getPhoneNumber())
                .province(address.getProvinceName())
                .district(address.getDistrictName())
                .ward(address.getWardName())
                .specificAddress(address.getDetailAddress())
                .build();
    }
}
