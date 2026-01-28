package com.greenwich.flowerplus.dto.snapshot;

import lombok.Builder;
import java.io.Serializable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Builder
public record ShippingAddressSnapshot(
        String fullName,
        String phoneNumber,
        String province,
        String district,
        String ward,
        String specificAddress
) implements Serializable {
    public String toFullString() {
        return Stream.of(specificAddress, ward, district, province)
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.joining(", "));
    }
}