package com.greenwich.flowerplus.dto.snapshot;

import com.greenwich.flowerplus.entity.ContactAddress;
import lombok.*;

import java.io.Serializable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        return Stream.of(specificAddress, ward, district, province)
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(", "));
    }
}
